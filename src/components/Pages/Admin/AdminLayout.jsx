import { Routes, Route } from "react-router-dom";

import DashBoard from "./DashBoard";
import AdminSidebar from "./AdminSidebar";


const AdminLayout = () => {
    return (
        <div className="flex">
            <AdminSidebar />
            <div style={{ flex: 1 }}>
                <Routes>
                    <Route path="/" element={<DashBoard />} />
                </Routes>
            </div>
        </div>
    );
};

export default AdminLayout;
