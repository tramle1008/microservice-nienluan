import React from "react";
import { Navigate } from "react-router-dom";

const AdminRoute = ({ children }) => {
    const auth = JSON.parse(localStorage.getItem("auth"));
    const roles = auth?.role || [];

    const isAdmin = roles.includes("ROLE_ADMIN");

    if (!isAdmin) {
        return <Navigate to="/" replace />;
    }

    return children;
};

export default AdminRoute;
