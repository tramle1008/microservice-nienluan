
import React from "react";
import { Link } from "react-router-dom";
const ViewOrderDetailButton = ({ orderId }) => {
    return (
        <Link
            to={`/admin/orders/detail/${orderId}`}
            className="inline-flex items-center gap-1 px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700 transition text-sm font-medium"
        >
            Xem chi tiết
        </Link>
    );
};

export default ViewOrderDetailButton;