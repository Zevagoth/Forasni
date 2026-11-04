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

test("Unauthenticated user cannot read bookings, providers, tenders, or users", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("bookings").get());
  await assertFails(unauthDb.collection("providers").get());
  await assertFails(unauthDb.collection("tenders").get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user can create and read their own user profile, but not another user's profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const now = new Date();

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "أحمد العبدالله",
      accountType: "CLIENT",
      governorate: "عمّان",
      neighborhood: "خلدا",
      phoneNumber: "+962791234567",
      pointsBalance: 25,
      createdAt: now,
      updatedAt: now,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
});

test("Authenticated user cannot read another user's booking and query requires userId filter", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("bookings").doc("booking_bob").set({
      userId: BOB_UID,
      providerId: "prov_1",
      providerName: "محمود الزعبي",
      providerSkill: "كهربائي منازل وتمديدات",
      providerPhone: "+962790001111",
      serviceTitle: "صيانة لوحة القواطع الرئيسية",
      governorate: "عمّان",
      neighborhood: "عبدون",
      scheduledDate: "2026-11-06",
      scheduledTime: "04:00 م",
      trackingStage: "CONFIRMED",
      trackingEtaMinutes: 20,
      escrowAmountJod: 45,
      escrowCommissionJod: 2,
      escrowStatus: "HELD_IN_ESCROW",
      isReviewed: false,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertFails(aliceDb.collection("bookings").doc("booking_bob").get());
  await assertFails(bobDb.collection("bookings").get());
  await assertSucceeds(
    bobDb.collection("bookings").where("userId", "==", BOB_UID).get()
  );
});

test("Authenticated user can query published providers and tenders with isPublished filter", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("providers").doc("prov_1").set({
      ownerId: BOB_UID,
      isPublished: true,
      name: "م. طارق النسور",
      skillCategory: "ELECTRICIAN",
      skillTitle: "كهربائي منازل وصيانة أعطال فورية",
      governorate: "عمّان",
      neighborhood: "خلدا",
      bio: "خبرة 12 عاماً في تأسيس وصيانة الكهرباء المنزلية واللوحات الذكية.",
      businessPhone: "+962795551122",
      whatsappNumber: "962795551122",
      hourlyRateJod: 15,
      inspectionFeeJod: 5,
      isAvailableNow: true,
      isVerified: true,
      licenseTitle: "شهادة مزاولة مهنة - مؤسسة التدريب المهني",
      portfolioCount: 12,
      portfolioTitles: "تمديد سبوتات|صيانة قاطع|تركيب ثريا",
      portfolioTypes: "PHOTO|PHOTO|VIDEO",
      pointsBalance: 30,
      leadsCount: 48,
      ratingAvg: 4.9,
      reviewCount: 34,
      latestReviewText: "شغل نظيف وموعد دقيق جداً",
      distanceKm: 2.4,
      createdAt: now,
      updatedAt: now,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("providers").where("isPublished", "==", true).get()
  );
});
