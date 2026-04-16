import { Navigate } from "react-router-dom";

const DeliverRoute = ({ children }) => {
    const auth = JSON.parse(localStorage.getItem("auth"));
    const roles = auth?.role || [];

    const isDeliver = roles.includes("ROLE_DELIVER");

    if (!isDeliver) {
        return <Navigate to="/" replace />;
    }

    return children;
}
export default DeliverRoute;