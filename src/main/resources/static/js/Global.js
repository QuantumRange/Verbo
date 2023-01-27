let user_data;


function toggleMark(element) {
    let id = element.getAttribute('alt');
    element.disabled = true;



    // Request /api/set/mark?id={id}
    const xhr = new XMLHttpRequest();
    let url = "/api/set/mark?id=" + id;
    xhr.open("POST", url, true);
    xhr.setRequestHeader("Content-Type", "application/json");

    xhr.onreadystatechange = function () {
        if (xhr.readyState === 4 && xhr.status === 200) {
            element.disabled = false;
            const res = element.innerHTML === "Unmark" ? "Mark" : "Unmark";

            let elements = document.getElementsByClassName("mark-" + id);

            for (let i = 0; i < elements.length; i++) {
                const unmarked = elements.item(i).innerHTML === "Unmark";

                if (!unmarked) {
                    elements.item(i).parentNode.parentNode.parentNode.classList.add("card-marked");
                } else {
                    elements.item(i).parentNode.parentNode.parentNode.classList.remove("card-marked");
                }

                elements.item(i).innerHTML = res;
            }
        }
    };

    xhr.send();
}

function updateSite(data) {
    user_data = data;

    let colors = data["COLOR_THEME"];
    let root = document.documentElement;

    console.log(colors);

    for (let key in colors) {
        root.style.setProperty(key, colors[key]);
    }
}

window.addEventListener('load', function () {
    initSetSelector();

    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]')
    const tooltipList = [...tooltipTriggerList].map(tooltipTriggerEl => new bootstrap.Tooltip(tooltipTriggerEl))

    tooltipList.forEach(tooltip => tooltip);
});

function modifySet(type, id) {
    const xmlhttp = new XMLHttpRequest();

    xmlhttp.open("POST", "/course/" + document.getElementById("courseID").value + "/edit/" + type, true);
    xmlhttp.setRequestHeader("Content-Type", "application/json");

    xmlhttp.onreadystatechange = function () {
        if (xmlhttp.readyState === 4 &&
            xmlhttp.status === 200) {
            location.reload();
        }
    };

    xmlhttp.send(id);
}

let map = {};

function initSetSelector() {
    const output = document.getElementById("selection");

    if (output === null) return;

    let names = [];

    let split = document.getElementById("selectionAll").value.split("@");

    for (let i = 0; i < split.length; i++) {
        const sp = split[i].split("~");

        map[sp[0]] = sp[1];
        names.push(sp[0]);

    }

    autocomplete(document.getElementById("selectorInput"), names);

    let setsDiv = document.getElementById("sets").children[0].children[0].children;

    for (let i = 0; i < setsDiv.length; i++) {
        const setDiv = setsDiv[i];

        setDiv.addEventListener("click", function(event) {
            modifySet("remove", setDiv.children[0].children[0].children[0].innerText);
        });
    }
}

function autocomplete(inp, arr) {
    let currentFocus;

    inp.addEventListener("input", function(e) {
        var a, b, i, val = this.value;

        closeAllLists();

        if (!val) return false;

        currentFocus = -1;

        a = document.createElement("DIV");
        a.setAttribute("id", this.id + "autocomplete-list");
        a.setAttribute("class", "autocomplete-items");

        this.parentNode.appendChild(a);

        for (i = 0; i < arr.length; i++) {
            if (arr[i].substr(0, val.length).toUpperCase() === val.toUpperCase()) {

                b = document.createElement("DIV");

                b.innerHTML = "<strong>" + arr[i].substr(0, val.length) + "</strong>";
                b.innerHTML += arr[i].substr(val.length);

                b.innerHTML += "<input type='hidden' value='" + arr[i] + "'>";

                b.addEventListener("click", function(e) {
                    const name = this.getElementsByTagName("input")[0].value;

                    modifySet("add", map[name]);

                    closeAllLists();
                });
                a.appendChild(b);
            }
        }
    });

    inp.addEventListener("keydown", function(e) {
        let x = document.getElementById(this.id + "autocomplete-list");
        if (x) x = x.getElementsByTagName("div");
        if (e.keyCode === 40) {
            currentFocus++;

            addActive(x);
        } else if (e.keyCode === 38) {
            currentFocus--;
            addActive(x);
        } else if (e.keyCode === 13) {
            e.preventDefault();
            if (currentFocus > -1) {
                if (x) x[currentFocus].click();
            }
        }
    });

    function addActive(x) {
        if (!x) return false;

        removeActive(x);
        if (currentFocus >= x.length) currentFocus = 0;
        if (currentFocus < 0) currentFocus = (x.length - 1);

        x[currentFocus].classList.add("autocomplete-active");
    }

    function removeActive(x) {
        for (let i = 0; i < x.length; i++) {
            x[i].classList.remove("autocomplete-active");
        }
    }
    function closeAllLists(elmnt) {
        const x = document.getElementsByClassName("autocomplete-items");
        for (let i = 0; i < x.length; i++) {
            if (elmnt !== x[i] && elmnt !== inp) {
                x[i].parentNode.removeChild(x[i]);
            }
        }
    }

    document.addEventListener("click", function (e) {
        closeAllLists(e.target);
    });
}