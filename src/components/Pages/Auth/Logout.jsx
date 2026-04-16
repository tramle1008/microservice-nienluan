import { useEffect } from "react";

const Logout = () => {
    useEffect(() => {
        localStorage.removeItem("auth");
        window.location.href = "/login";
    }, []);

    return null;
};

export default Logout;
