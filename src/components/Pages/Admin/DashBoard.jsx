import { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchProduct } from "../../../store/actions";
import { Link } from "react-router-dom";
import api from "../../../api/api";
import DailyOrderChart from "./DailyOrderChart";

const Dashboard = () => {
    const [stats, setStats] = useState(null);
    const { products } = useSelector((state) => state.products);
    const dispatch = useDispatch();

    useEffect(() => {
        const token = JSON.parse(localStorage.getItem("auth"))?.jwtToken;

        api.get("/admin/dashboard", {
            headers: { Authorization: `Bearer ${token}` },
        })
            .then(res => setStats(res.data))
            .catch(err => console.error("Lỗi lấy thống kê:", err));

        dispatch(fetchProduct());
    }, [dispatch]);

    if (!stats) {
        return (
            <div className="min-h-screen bg-[#f1f7f0] flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-16 w-16 border-t-4 border-emerald-700 mx-auto"></div>
                    <p className="mt-6 text-2xl text-gray-700 font-medium">Đang tải dữ liệu...</p>
                </div>
            </div>
        );
    }

    // Lọc variant sắp hết hàng (< 5)
    const lowStockVariants = products
        ? products.flatMap(product =>
            (product.variants || [])
                .filter(variant => variant.stockQuantity < 5)
                .map(variant => ({
                    productId: product.productId,
                    productName: product.productName,
                    variantId: variant.variantId,
                    color: variant.color,
                    stockQuantity: variant.stockQuantity,
                    imageUrl: variant.imageUrl || product.imageUrl,
                    price: variant.finalPrice || product.finalPrice || product.price
                }))
        )
        : [];

    const sortedLowStock = [...lowStockVariants].sort((a, b) => a.stockQuantity - b.stockQuantity);
    const outOfStockCount = lowStockVariants.filter(v => v.stockQuantity === 0).length;

    return (
        <div className="p-6 bg-gray-50 min-h-screen">
            <h1 className="text-3xl font-bold text-gray-800 mb-8">Dashboard Quản Trị</h1>

            {/* 4 Ô THỐNG KÊ - GIỮ NGUYÊN STYLE CŨ CỦA StatCard */}
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-10">
                <Link to="#">
                    <StatCard
                        label="Tổng khách hàng"
                        value={stats.totalUsers?.toLocaleString() || 0}
                    />
                </Link>
                <Link to="/admin/product">
                    <StatCard
                        label="Tổng sản phẩm"
                        value={stats.totalProducts?.toLocaleString() || 0}
                    />
                </Link>
                <Link to="/admin/orders">
                    <StatCard
                        label="Đơn hàng mới"
                        value={stats.pendingOrders || 0}
                        highlight={stats.pendingOrders > 0}
                    />
                </Link>
                <StatCard label="Doanh thu hôm nay / Tổng" >
                    <div className="space-y-1">
                        <div className="text-2xl md:text-3xl font-bold">
                            {(stats.todayRevenue ?? 0).toLocaleString('vi-VN')} ₫  / {(stats.totalRevenue || 0).toLocaleString('vi-VN')} ₫
                        </div>
                    </div>
                </StatCard>
            </div>
            {/* BIỂU ĐỒ ĐƯỜNG - ĐƠN HÀNG TUẦN HIỆN TẠI */}
            <div className="bg-white rounded-lg shadow-lg p-6 mb-10">
                <DailyOrderChart />
            </div>
            {/* BẢNG CẢNH BÁO TỒN KHO - GIỮ NGUYÊN NHƯ BẠN */}
            <div className="bg-white rounded-lg shadow-lg overflow-hidden">
                <div className="bg-red-50 px-6 py-4 border-b border-red-200">
                    <h2 className="text-xl font-bold text-red-700">
                        Cảnh báo tồn kho ({sortedLowStock.length} sản phẩm cần nhập thêm
                        {outOfStockCount > 0 && ` - ${outOfStockCount} đã HẾT HÀNG`})
                    </h2>
                </div>

                <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-100">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">STT</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Ảnh</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-600 uppercase tracking-wider">Sản phẩm</th>
                                <th className="px-6 py-3 text-center text-xs font-medium text-gray-600 uppercase tracking-wider">Giá</th>
                                <th className="px-6 py-3 text-center text-xs font-medium text-gray-600 uppercase tracking-wider">Tồn kho</th>
                                <th className="px-6 py-3 text-center text-xs font-medium text-gray-600 uppercase tracking-wider">Hành động</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {sortedLowStock.length > 0 ? (
                                sortedLowStock.map((item, index) => (
                                    <tr key={item.variantId} className="hover:bg-gray-50 transition">
                                        <td className="px-6 py-4 text-center font-semibold text-gray-700">{index + 1}</td>
                                        <td className="px-6 py-4">
                                            <img
                                                src={item.imageUrl || "/placeholder.jpg"}
                                                alt={item.productName}
                                                className="w-16 h-16 object-cover rounded-lg shadow-sm"
                                            />
                                        </td>
                                        <td className="px-6 py-4 max-w-xs">
                                            <div className="font-semibold text-gray-800">{item.productName}</div>
                                            {item.color && (
                                                <div className="text-sm text-blue-600 mt-1">Phân loại: {item.color}</div>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 text-center font-medium tabular-nums">
                                            {Number(item.price).toLocaleString('vi-VN')} ₫
                                        </td>
                                        <td className={`px-6 py-4 text-center text-lg font-bold ${item.stockQuantity === 0 ? 'text-red-600' : item.stockQuantity <= 2 ? 'text-orange-600' : 'text-yellow-600'}`}>
                                            {item.stockQuantity === 0 ? 'HẾT HÀNG' : item.stockQuantity}
                                        </td>
                                        <td className="px-6 py-4 text-center">
                                            <Link
                                                to={`/admin/product/update/${item.productId}`}
                                                className="inline-block bg-gray-600 text-white px-4 py-2 rounded-md hover:bg-gray-700 text-sm font-medium transition"
                                            >
                                                Nhập thêm hàng
                                            </Link>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="6" className="px-6 py-12 text-center text-gray-500 text-lg">
                                        Tất cả sản phẩm đều còn đủ hàng
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

// STATCARD GIỮ NGUYÊN STYLE CŨ (chỉ thêm hỗ trợ children)
const StatCard = ({ label, value, children, icon, highlight = false }) => (
    <div style={{
        background: "#f9f9f9",
        padding: "20px",
        borderRadius: "12px",
        boxShadow: "0 0 10px rgba(0,0,0,0.05)",
        textAlign: "center",
        border: highlight ? "2px solid #9DC183" : "none",
    }}>
        {icon && <div style={{ fontSize: "2.5rem", marginBottom: "10px" }}>{icon}</div>}

        <h2 style={{
            fontSize: "24px",
            marginBottom: "10px",
            fontWeight: "bold",
            color: highlight ? "#ef4444" : "#333"
        }}>
            {children ? children : value}
        </h2>

        <p style={{ fontSize: "16px", color: "#555" }}>{label}</p>
    </div>
);

export default Dashboard;