// A little bit of code to make the task checkmarks sticky.

function getCookie(cname) {
    let name = cname + "=";
    let decodedCookie = decodeURIComponent(document.cookie);
    let ca = decodedCookie.split(';');
    for(let i = 0; i <ca.length; i++) {
        let c = ca[i];
        while (c.charAt(0) == ' ') {
            c = c.substring(1);
        }
        if (c.indexOf(name) == 0) {
            return c.substring(name.length, c.length);
        }
    }
    return "";
}

function setCookie(cname, cvalue, exdays) {
    const d = new Date();
    d.setTime(d.getTime() + (exdays*24*60*60*1000));
    let expires = "expires="+ d.toUTCString();
    document.cookie = cname + "=" + cvalue + ";" + expires + ";path=/";
}

if (typeof tutorialSection !== 'undefined') {
    let ticks = Array.from(document.querySelectorAll(".task-list-control > input[type=checkbox]"));

    var initialCheckedIndexes = getCookie("completed-tasks").split(",");

    // Each checkbox is given a global id formed from the index in the page, plus the index of the tutorial section * 100.
    var globalTickIDStart = tutorialSection * 100;
    var globalTickIDEnd = (tutorialSection + 1) * 100;

    for (var i = 0; i < ticks.length; i++) {
        let cb = ticks[i];
        cb.removeAttribute("disabled");
        if (!initialCheckedIndexes.includes("" + (globalTickIDStart + i)))
            cb.removeAttribute("checked");
        cb.addEventListener("click", function (e) {
            var nowCheckedIDs = ticks.map((control, index) => {
                if (control.checked) {
                    return globalTickIDStart + index
                } else {
                    return -1
                }
            }).filter(el => el != -1);

            nowCheckedIDs = nowCheckedIDs.concat(initialCheckedIndexes.map(numStr => parseInt(numStr)).filter(num => num < globalTickIDStart || num >= globalTickIDEnd));

            setCookie("completed-tasks", nowCheckedIDs.join(","));
        });
    }
}
