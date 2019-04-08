const REFRESH_PERIOD = 1000;
const ROOT_TEMPLATE_ID = "rootTemplate";
const ROOT_ID = "root";
const MAIN_BODY_WRAPPER_ID = "mainBodyWrapper";

HandlebarsIntl.registerWith(Handlebars);

function refresh() {
    try {
        var alertData = getAlertData();
        var statsData = getStatsData();
        var data = compileData(alertData, statsData);
        render(data);
    }
    catch (error) {
        console.error("An error occurred while fetching data from the server:");
        console.error(error);
    }
    setTimeout(refresh, REFRESH_PERIOD);
}

function getAlertData() {
    return getApiData("alerts");
}

function getStatsData() {
    return getApiData("stats");
}

function getApiData(subPath) {
    var request = new XMLHttpRequest();
    request.open("GET", "/api/" + subPath, false);
    request.send(null);
    return JSON.parse(request.responseText);
}

function compileData(alertData, statsData) {
	statsData['requestsWithErrors'] = statsIncludeRequestsWithErrors(statsData);
	statsData['sortedErrorCodes'] = sortedErrorCodes(statsData);
    return {
        onGoingAlert: onGoingAlert(alertData),
        alerts: alertData,
        statsPresent: statsPresent(statsData),
        stats: statsData
    };
}

function onGoingAlert(alerts) {
    return alerts && alerts.length > 0 && alerts[alerts.length - 1].type == "PEAK_TRAFFIC_START";
}

function statsPresent(stats) {
	return stats && Object.entries(stats.sectionHits).length > 0;
}

function statsIncludeRequestsWithErrors(stats) {
    return stats && stats.errors && Object.entries(stats.errors).length > 0;
}

function sortedErrorCodes(stats) {
	if (!statsIncludeRequestsWithErrors(stats)) {
		return [];
	}
	return Object.keys(stats.errors).sort().reverse();
}

function render(data) {
    let top = 0, left = 0;
    let mainBodyWrapper = getMainBodyWrapper();
    if (mainBodyWrapper) {
        top = mainBodyWrapper.scrollTop;
        left = mainBodyWrapper.scrollLeft;
    }
    if (document.getElementById(ROOT_TEMPLATE_ID) && document.getElementById(ROOT_ID)) {
        var template = Handlebars.compile(document.getElementById(ROOT_TEMPLATE_ID).innerHTML);
        document.getElementById(ROOT_ID).innerHTML = template(data);
    }
    mainBodyWrapper = getMainBodyWrapper();
    if (mainBodyWrapper) {
        mainBodyWrapper.scrollTo(left, top);
    }
}

function getMainBodyWrapper() {
    return document.getElementById(MAIN_BODY_WRAPPER_ID);
}

refresh();
