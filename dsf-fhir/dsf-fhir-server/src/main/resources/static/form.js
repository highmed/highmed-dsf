function completeQuestionnaireResponse() {
    const questionnaireResponseStringBefore = document.getElementById("json").innerText
    const questionnaireResponse = JSON.parse(questionnaireResponseStringBefore)

    const errors = []
    readAnswersFromForm(questionnaireResponse, errors)

    console.log(questionnaireResponse)
    console.log(errors)

    if (errors.length === 0) {
        const questionnaireResponseStringAfter = JSON.stringify(questionnaireResponse)
        updateQuestionnaireResponse(questionnaireResponseStringAfter)
    }
}

function readAnswersFromForm(questionnaireResponse, errors) {
    questionnaireResponse.status = "completed";

    questionnaireResponse.item.forEach((item) => {
        const id = item.linkId
        const answer = item.answer[0]
        const answerType = Object.keys(answer)[0]

        if (id !== "business-key" && id !== "user-task-id") {
            answer[answerType] = readAndValidateValue(id, answerType, errors)
        }
    })
}

function readAndValidateValue(id, answerType, errors) {
    const value = document.getElementById(id).value

    const rowElement = document.getElementById(id + "-row");
    const errorListElement = document.getElementById(id + "-error");
    errorListElement.replaceChildren()

    if (answerType === 'valueString') {
        return validateString(rowElement, errorListElement, value, errors, id)
    } else if (answerType === 'valueInteger') {
        return validateInteger(rowElement, errorListElement, value, errors, id)
    } else if (answerType === 'valueDecimal') {
        return validateDecimal(rowElement, errorListElement, value, errors, id)
    } else if (answerType === 'valueDate') {
        return validateDate(rowElement, errorListElement, value, errors, id)
    } else if (answerType === 'valueTime') {
        return validateTime(rowElement, errorListElement, value, errors, id)
    } else if (answerType === 'valueDateTime') {
        return validateDateTime(rowElement, errorListElement, value, errors, id)
    } else if (answerType === 'valueUri') {
        return validateUrl(rowElement, errorListElement, value, errors, id)
    } else if (answerType === 'valueReference') {
        return validateReference(rowElement, errorListElement, value, errors, id)
    } else if (answerType === 'valueBoolean') {
        return document.querySelector("input[name=" + id + "]:checked").value
    } else {
        return null
    }
}

function validateString(rowElement, errorListElement, value, errors, id) {
    if (value === null || value.trim() === "") {
        addError(rowElement, errorListElement, errors, id, "Value is null or empty")
        return null
    } else {
        removeError(rowElement, errorListElement)
        return value
    }
}

function validateInteger(rowElement, errorListElement, value, errors, id) {
    validateString(rowElement, errorListElement, value, errors, id)

    if (!Number.isInteger(parseInt(value))) {
        addError(rowElement, errorListElement, errors, id, "Value is not an integer")
        return null
    } else {
        removeError(rowElement, errorListElement)
        return value
    }
}

function validateDecimal(rowElement, errorListElement, value, errors, id) {
    validateString(rowElement, errorListElement, value, errors, id)

    if (isNaN(parseFloat(value))) {
        addError(rowElement, errorListElement, errors, id, "Value is not a decimal")
        return null
    } else {
        removeError(rowElement, errorListElement)
        return value
    }
}

function validateDate(rowElement, errorListElement, value, errors, id) {
    validateString(rowElement, errorListElement, value, errors, id)

    const date = new Date(value)
    if ((date === "Invalid Date") || isNaN(date)) {
        addError(rowElement, errorListElement, errors, id, "Value is not a date")
        return null
    } else {
        removeError(rowElement, errorListElement)
        return value
    }
}

function validateTime(rowElement, errorListElement, value, errors, id) {
    validateString(rowElement, errorListElement, value, errors, id)

    if (!(new RegExp('^([0-9]|0[0-9]|1[0-9]|2[0-3]):[0-5][0-9]$').test(value))) {
        addError(rowElement, errorListElement, errors, id, "Value is not a time")
        return null
    } else {
        removeError(rowElement, errorListElement)
        return value + ":00"
    }
}

function validateDateTime(rowElement, errorListElement, value, errors, id) {
    validateString(rowElement, errorListElement, value, errors, id)

    try {
        const dateTime = new Date(value).toISOString()
        removeError(rowElement, errorListElement)
        return dateTime
    } catch (_) {
        addError(rowElement, errorListElement, errors, id, "Value is not a date time")
        return null
    }
}

function validateReference(rowElement, errorListElement, value, errors, id) {
    validateString(rowElement, errorListElement, value, errors, id)

    try {
        new URL(value);
        removeError(rowElement, errorListElement)
        return {reference: value}
    } catch (_) {
        addError(rowElement, errorListElement, errors, id, "Value is not a reference")
        return null
    }
}

function validateUrl(rowElement, errorListElement, value, errors, id) {
    validateString(rowElement, errorListElement, value, errors, id)

    try {
        new URL(value);
        removeError(rowElement, errorListElement)
        return value
    } catch (_) {
        addError(rowElement, errorListElement, errors, id, "Value is not a url")
        return null
    }
}

function addError(rowElement, errorListElement, errors, id, message) {
    errors.push({id: id, error: message})

    rowElement.classList.add("error")

    const errorMessageElement = document.createElement("li");
    errorMessageElement.appendChild(document.createTextNode(message));

    errorListElement.appendChild(errorMessageElement);
    errorListElement.classList.remove("error-list-not-visible");
    errorListElement.classList.add("error-list-visible");
}

function removeError(rowElement, errorListElement) {
    rowElement.classList.remove("error");

    errorListElement.classList.remove("error-list-visible");
    errorListElement.classList.add("error-list-not-visible");
    errorListElement.replaceChildren()
}

function updateQuestionnaireResponse(questionnaireResponse) {
    const fullUrl = window.location.origin + window.location.pathname
    const url = fullUrl.slice(0, fullUrl.indexOf("/_history") + 1)

    enableSpinner()

    fetch(url, {
        method: "PUT",
        headers: {
            'Content-type': 'application/json'
        },
        body: questionnaireResponse
    }).then(response => {
        console.log(response)

        if (response.ok) {
            disableSpinner()
            window.scrollTo(0, 0);
            location.reload();
        } else {
            const status = response.status
            const statusText = response.statusText === null ? " - " + response.statusText : ""

            response.text().then((responseText) => {
                const alertText = "Status: " + status + statusText + "\n\n" + responseText.replace(/<!--.*?-->/sg, "")
                window.alert(alertText);
                disableSpinner()
            })
        }
    })
}

function enableSpinner() {
    const spinner = document.getElementById("spinner")
    spinner.classList.remove("spinner-disabled")
    spinner.classList.add("spinner-enabled")
}

function disableSpinner() {
    const spinner = document.getElementById("spinner")
    spinner.classList.remove("spinner-enabled")
    spinner.classList.add("spinner-disabled")
}