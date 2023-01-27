function inputPassword(id) {
    let passwordElement = document.getElementById(id);

    if (passwordElement.type === "password") {
        passwordElement.type = "text";
       //  passwordElement.className = "fa-solid fa-eye fromEye col-sm-1";
    } else {
        passwordElement.type = "password";
        // passwordElement.className = "fa-regular fa-eye fromEye col-sm-1";
    }
}