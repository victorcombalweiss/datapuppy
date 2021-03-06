const REFRESH_PERIOD = 1000;
const ROOT_TEMPLATE_ID = "rootTemplate";
const ROOT_ID = "root";
const MAIN_BODY_WRAPPER_ID = "mainBodyWrapper";

const PEAK_TRAFFIC_START_ALERT_TYPE = "PEAK_TRAFFIC_START";
const PEAK_TRAFFIC_STOP_ALERT_TYPE = "PEAK_TRAFFIC_STOP";

HandlebarsIntl.registerWith(Handlebars);

Handlebars.registerHelper("ifIsAlertStart", function(alertType, options) {
    return alertType == PEAK_TRAFFIC_START_ALERT_TYPE
        ? options.fn(this) : options.inverse(this);
});

Handlebars.registerHelper("formatFileSize", function(value) {
    if (typeof value === 'undefined') {
      return null;
    }
    for (const unit of ["B", "kB", "MB", "GB", "TB"]) {
      if (value < 1024) {
        return Math.floor(value) + " " + unit;
      }
      value /= 1024;
    }
    return filesize;
});

Handlebars.registerHelper('pluralize', function(number, singular, plural) {
    if (number === 1)
        return singular;
    else
        return (typeof plural === 'string' ? plural : singular + 's');
});

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
    return getApiData("alerts", []);
}

function getStatsData() {
    return getApiData("stats");
}

function getApiData(subPath, defaultValue) {
    var request = new XMLHttpRequest();
    request.open("GET", "/api/" + subPath, false);
    request.send(null);
    if (request.status != 200 && defaultValue) {
        return defaultValue;
    }
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
    return alerts && alerts.length > 0
        && alerts[0].type == PEAK_TRAFFIC_START_ALERT_TYPE;
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
    setSummaryStatsColumnSizes();
}

function getMainBodyWrapper() {
    return document.getElementById(MAIN_BODY_WRAPPER_ID);
}

refresh();

/********************* Display-specific logic *********************/

function setSummaryStatsColumnSizes() {
    const cellsToResize = document.querySelectorAll(".summaryStats .tableCell:first-child + .tableCell");
    for (const cell of cellsToResize) {
        const siblingWidth = cell.previousElementSibling.clientWidth;
        const parentWidth = cell.parentElement.clientWidth;
        const horizontalPadding = getComputedHorizontalPadding(cell);
        cell.style.width = parentWidth / 2 - siblingWidth - horizontalPadding;
    }
}

function getComputedHorizontalPadding(element) {
    const computedStyle = window.getComputedStyle(element);
    const paddingLeft = computedStyle.getPropertyValue("padding-left");
    const paddingRight = computedStyle.getPropertyValue("padding-right");
    return pixelStringToInt(paddingLeft)
        + pixelStringToInt(paddingRight);
}

function pixelStringToInt(pixelString) {
    return parseInt(pixelString.substr(0, pixelString.length - 2));
}

window.onresize = setSummaryStatsColumnSizes;
