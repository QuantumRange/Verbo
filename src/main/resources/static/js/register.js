window.addEventListener('load', function () {
    const formDiv = document.getElementById("registerForm");
    const alertDiv = document.getElementById("infoAlert");
    alertDiv.style.display = "none";

    function setAlertText(msg) {
        const message = document.getElementById("infoMessage");

        alertDiv.style.display = "";
        message.innerText = msg;
    }

    formDiv.addEventListener('submit', function (event) {
        event.preventDefault();
        const xmlhttp = new XMLHttpRequest();

        let usernameInput = document.getElementById("username");
        let visibleNameInput = document.getElementById("name");
        let passwordInput = document.getElementById("password");
        let repeatCodeInput = document.getElementById("repeatPassword");
        let inviteCodeInput = document.getElementById("inviteCode");

        xmlhttp.open("POST", "/register");
        xmlhttp.setRequestHeader("Content-Type", "application/json");

        xmlhttp.onreadystatechange = function () {
            if (xmlhttp.readyState === 4 &&
                xmlhttp.status === 200) {
                const msg = xmlhttp.responseText;

                if (msg.startsWith("success:")) {
                    window.location = "/login?username=" + msg.split(":")[1];
                } else {
                    setAlertText(msg);
                }
            }
        };

        xmlhttp.send(JSON.stringify({
            "username": usernameInput.value,
            "nickname": visibleNameInput.value,
            "password": passwordInput.value,
            "repeatPassword": repeatCodeInput.value,
            "inviteCode": inviteCodeInput.value
        }));
    });
});