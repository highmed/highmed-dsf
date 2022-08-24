function completeQuestionnaireResponse() {
    const questionnaireResponseStringBefore = document.getElementById("json").innerText
    const questionnaireResponse = JSON.parse(questionnaireResponseStringBefore)

    readAnswersFromForm(questionnaireResponse)

    console.log(questionnaireResponse)
    const questionnaireResponseStringAfter = JSON.stringify(questionnaireResponse)

    updateQuestionnaireResponse(questionnaireResponseStringAfter)
}

function readAnswersFromForm(questionnaireResponse) {
    questionnaireResponse.status = "completed";

    questionnaireResponse.item.forEach((item) => {
        const id = item.linkId
        const answer = item.answer[0]
        const answerType = Object.keys(answer)[0]

        if (id !== "business-key" && id !== "user-task-id") {
            answer[answerType] = readValue(id, answerType)
        }
    })
}

function readValue(id, answerType) {
    if (['valueString', 'valueInteger', 'valueDecimal', 'valueDate', 'valueReference'].includes(answerType)) {
        return document.getElementById(id).value
    } else if (answerType === 'valueTime') {
        return document.getElementById(id).value + ":00"
    } else if (answerType === 'valueDateTime') {
        return new Date(document.getElementById(id).value).toISOString()
    } else if (answerType === 'valueBoolean') {
        return document.querySelector("input[name=" + id + "]:checked").value
    } else {
        return "unknown"
    }
}

function updateQuestionnaireResponse(questionnaireResponse) {
    const fullUrl = window.location.origin + window.location.pathname
    const url = fullUrl.slice(0, fullUrl.indexOf("/_history") + 1)

    fetch(url, {
        method: "PUT",
        headers: {
            'Content-type': 'application/json'
        },
        body: questionnaireResponse
    }).then(response => {
        console.log(response)
    })
}