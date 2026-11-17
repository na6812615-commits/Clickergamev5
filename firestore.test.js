const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validEntry(uid, overrides = {}) {
  const pastTime = new Date(Date.now() - 2000);
  return {
    userId: uid,
    displayName: "Cyber Tapper",
    clicks: 250,
    lifetimePoints: 15000,
    bestCombo: 42,
    restartLv: 1,
    reactorCode: "Mk.II",
    hasVipPass: false,
    createdAt: pastTime,
    updatedAt: pastTime,
    ...overrides,
  };
}

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot list or get leaderboard", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validEntry(ALICE_UID));
  });

  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("leaderboard").limit(50).get());
  await assertFails(unauthDb.collection("leaderboard").doc(ALICE_UID).get());
});

test("Unauthenticated user: cannot write leaderboard", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("leaderboard").doc("anon_1").set(validEntry("anon_1"))
  );
});

test("Authenticated user: can create and update own leaderboard entry", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("leaderboard").doc(ALICE_UID);

  await assertSucceeds(docRef.set(validEntry(ALICE_UID)));

  await assertSucceeds(
    docRef.update({
      clicks: 900,
      lifetimePoints: 88000,
      bestCombo: 65,
      restartLv: 2,
      reactorCode: "Mk.III",
      hasVipPass: true,
      displayName: "Alice Pro",
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Authenticated user: cannot create or update another user's leaderboard entry", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("leaderboard")
      .doc(BOB_UID)
      .set(validEntry(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("leaderboard").doc(BOB_UID).set(validEntry(BOB_UID))
  );
  await assertFails(
    aliceDb.collection("leaderboard").doc(BOB_UID).update({ clicks: 999999 })
  );
});

test("Authenticated user: cannot delete another user's leaderboard entry", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("leaderboard")
      .doc(BOB_UID)
      .set(validEntry(BOB_UID));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("leaderboard").doc(BOB_UID).delete());
});

test("Authenticated user: cannot spoof userId or add ghost fields or negative scores", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("leaderboard").doc(ALICE_UID);

  await assertFails(
    docRef.set(validEntry(ALICE_UID, { userId: BOB_UID }))
  );
  await assertFails(
    docRef.set(validEntry(ALICE_UID, { adminFlag: true }))
  );
  await assertFails(
    docRef.set(validEntry(ALICE_UID, { clicks: -10 }))
  );
  await assertFails(
    docRef.set(validEntry(ALICE_UID, { displayName: "X".repeat(41) }))
  );
});

test("Authenticated user: cannot mutate createdAt on update", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("leaderboard").doc(ALICE_UID);

  await assertSucceeds(docRef.set(validEntry(ALICE_UID)));

  await assertFails(
    docRef.update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );
});

test("Authenticated user: can query global top leaderboard ordered by clicks and lifetimePoints", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context
      .firestore()
      .collection("leaderboard")
      .doc(ALICE_UID)
      .set(validEntry(ALICE_UID, { clicks: 500, lifetimePoints: 40000 }));
    await context
      .firestore()
      .collection("leaderboard")
      .doc(BOB_UID)
      .set(validEntry(BOB_UID, { clicks: 1200, lifetimePoints: 95000 }));
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("leaderboard").orderBy("clicks", "desc").limit(50).get()
  );
  await assertSucceeds(
    aliceDb
      .collection("leaderboard")
      .orderBy("lifetimePoints", "desc")
      .limit(50)
      .get()
  );
  await assertFails(
    aliceDb.collection("leaderboard").orderBy("clicks", "desc").limit(150).get()
  );
});
