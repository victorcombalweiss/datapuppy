const REFRESH_PERIOD = 1000;
const ROOT_TEMPLATE_ID = "rootTemplate";
const ROOT_ID = "root";

function refresh() {
    try {
        var alertData = getAlertData();
        var data = compileData(alertData);
        render(data);
    }
    catch (error) {
        console.error("An error occurred while fetching data from the server:");
        console.error(error);
    }
    setTimeout(refresh, REFRESH_PERIOD);
}

function getAlertData() {
    var request = new XMLHttpRequest();
    request.open("GET", "/api/alerts", false);
    request.send(null);
    return JSON.parse(request.responseText);
}

function compileData(alertData) {
    return {
        onGoingAlert: onGoingAlert(alertData),
        alerts: alertData
    };
}

function onGoingAlert(alerts) {
    return alerts && alerts.length > 0 && alerts[alerts.length - 1].type == "PEAK_TRAFFIC_START";
}

function render(data) {
    if (document.getElementById(ROOT_TEMPLATE_ID) && document.getElementById(ROOT_ID)) {
        var template = Handlebars.compile(document.getElementById(ROOT_TEMPLATE_ID).innerHTML);
        document.getElementById(ROOT_ID).innerHTML = template(data);
    }
}

refresh();
