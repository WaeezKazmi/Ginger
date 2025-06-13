const functions = require("firebase-functions");
const admin = require("firebase-admin");
admin.initializeApp();

exports.sendEventNotification = functions.database
    .ref("/shared_events/{eventId}")
    .onCreate(async (snapshot, context) => {
        try {
            const event = snapshot.val();
            const tokensSnapshot = await admin.database()
                .ref("users")
                .once("value");
            const tokens = [];
            tokensSnapshot.forEach((user) => {
                const token = user.child("fcmToken").val();
                if (token && user.key !== event.creatorId) {
                    tokens.push(token);
                }
            });

            if (tokens.length === 0) {
                console.log("No valid FCM tokens found");
                return null;
            }

            const payload = {
                data: {
                    eventId: event.id.toString(),
                    title: event.title,
                    description: event.description,
                    date: event.date,
                    imageUrl: event.imageUrl || "",
                    creatorType: event.creatorType || "",
                    creatorName: event.creatorName || "",
                    creatorId: event.creatorId || "",
                    creatorDesignation: event.creatorDesignation || "",
                    creatorDepartment: event.creatorDepartment || "",
                    creatorPosition: event.creatorPosition || "",
                    eventKey: context.params.eventId,
                },
            };

            const response = await admin.messaging().sendMulticast({
                tokens: tokens,
                data: payload.data,
            });

            console.log("FCM message sent successfully:", response);
            return null;
        } catch (error) {
            console.error("Error sending FCM message:", error);
            return null;
        }
    });
