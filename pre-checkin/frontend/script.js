const form = document.getElementById("preCheckinForm");

const submitButton =
    document.getElementById("submitButton");

const formMessage =
    document.getElementById("formMessage");


const API_URL =
    "http://localhost:8080/api/pre-checkin";


form.addEventListener("submit", async function (event) {

    event.preventDefault();

    clearMessage();

    submitButton.disabled = true;

    submitButton.textContent = "Submitting...";


    const request = {

        reservationNumber:
            document.getElementById("reservationNumber").value.trim(),

        roomNumber:
            document.getElementById("roomNumber").value.trim(),

        firstName:
            document.getElementById("firstName").value.trim(),

        lastName:
            document.getElementById("lastName").value.trim(),

        email:
            document.getElementById("email").value.trim(),

        mobileNumber:
            document.getElementById("mobileNumber").value.trim(),

        arrivalDate:
            document.getElementById("arrivalDate").value,

        departureDate:
            document.getElementById("departureDate").value,

        estimatedArrivalTime:
            document.getElementById("estimatedArrivalTime").value,

        nationality:
            document.getElementById("nationality").value.trim(),

        idType:
            document.getElementById("idType").value,

        idNumber:
            document.getElementById("idNumber").value.trim()
    };


    console.log("Sending pre-check-in request:", request);


    try {

        const response = await fetch(API_URL, {

            method: "POST",

            headers: {
                "Content-Type": "application/json"
            },

            body: JSON.stringify(request)
        });


        if (!response.ok) {

            let errorMessage =
                "Unable to complete pre-check-in.";

            try {

                const errorResponse =
                    await response.json();

                if (errorResponse.message) {
                    errorMessage =
                        errorResponse.message;
                }

            } catch (error) {

                console.error(
                    "Unable to parse error response:",
                    error
                );
            }

            throw new Error(errorMessage);
        }


        const result =
            await response.json();


        console.log(
            "Pre-check-in successful:",
            result
        );


        showMessage(
            "Pre-check-in completed successfully.",
            "success"
        );


        form.reset();

    }
    catch (error) {

        console.error(
            "Pre-check-in request failed:",
            error
        );


        showMessage(
            error.message ||
            "Something went wrong. Please try again.",
            "error"
        );

    }
    finally {

        submitButton.disabled = false;

        submitButton.textContent =
            "Complete Pre-Check-in";
    }

});


function showMessage(message, type) {

    formMessage.textContent = message;

    formMessage.className =
        "form-message " + type;
}


function clearMessage() {

    formMessage.textContent = "";

    formMessage.className =
        "form-message";
}
