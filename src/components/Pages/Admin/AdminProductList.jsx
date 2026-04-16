import React, { useEffect, useMemo, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import { fetchProduct } from "../../../store/actions";
import { Link, useSearchParams } from "react-router-dom";
import PaginationRounded from "../../PaginationRounded";
import AdminSidebar from "./AdminSidebar";
import { MdDeleteOutline } from "react-icons/md";
import { FaEdit } from "react-icons/fa";
import api from "../../../api/api";
import DiscountSelector from "./Discount/DiscountSelector";

const AdminProductList = () => {
    const productState = useSelector((state) => state.products || {});
    const products = productState.products || [];
    const pagination = productState.pagination || {};

    const {
        pageNumber = 0,
        pageSize = 5,
        totalElements = 0,
        totalPages = 1,
        lastPage = false
    } = pagination;

    const dispatch = useDispatch();
    const [searchParams] = useSearchParams();
    const page = Number(searchParams.get("page")) || 1;

    useEffect(() => {
        const pageIndex = page - 1;
        dispatch(fetchProduct(null, `pageNumber=${pageIndex}&pageSize=5&sortBy=productId&sortOrder=asc`));
    }, [dispatch, page]);
    const [discounts, setDiscounts] = useState([]);
    const [loadingDiscount, setLoadingDiscount] = useState(false);

    useEffect(() => {
        const loadDiscounts = async () => {
            try {
                setLoadingDiscount(true);
                const { data } = await api.get("/discounts/public/active"); // THÊM await
                setDiscounts(data);
            } catch (err) {
                console.error("Lỗi load khuyến mãi:", err);
            } finally {
                setLoadingDiscount(false);
            }
        };
        loadDiscounts();
    }, []);
    // FIX: Toàn bộ useMemo này phải nằm trong component, không được có code thừa
    const variantRows = useMemo(() => {
        const rows = [];

        products.forEach((product) => {
            const baseName = product.productName;
            const baseImage = product.imageUrl;
            const basePrice = product.price;
            const productFinalPrice = product.finalPrice;
            const productDiscounts = product.appliedDiscounts || [];

            // Lấy discount tốt nhất: ưu tiên variant > product
            const getBestDiscount = (variantDiscounts = []) => {
                if (variantDiscounts && variantDiscounts.length > 0) {
                    return variantDiscounts[0];
                }
                if (productDiscounts.length > 0) {
                    return productDiscounts[0];
                }
                return null;
            };

            const renderDiscountBadge = (discount) => {
                if (!discount) return null;
                return (
                    <span className="inline-block px-2 py-1 text-xs font-bold text-white bg-red-400 rounded-md">
                        -{discount.percentage}% {discount.name}
                    </span>
                );
            };

            // Trường hợp không có variant
            if (!product.variants || product.variants.length === 0) {
                const discount = getBestDiscount();
                rows.push({
                    variantId: null,
                    productId: product.productId,
                    displayName: baseName,
                    color: "-",
                    imageUrl: baseImage,
                    price: basePrice,
                    finalPrice: productFinalPrice,
                    stock: 0,
                    discount,
                    discountBadge: renderDiscountBadge(discount),
                });
                return;
            }

            // Có variant → duyệt từng cái
            product.variants.forEach((variant) => {
                const color = variant.color?.trim() ? variant.color : "Mặc định";
                const displayName = `${baseName} (${color})`;

                const price = variant.priceOverride ?? basePrice;
                const finalPrice = variant.finalPrice ?? productFinalPrice;
                const bestDiscount = getBestDiscount(variant.appliedDiscounts);

                rows.push({
                    variantId: variant.variantId,
                    productId: product.productId,
                    displayName,
                    color,
                    imageUrl: variant.imageUrl || baseImage,
                    price,
                    finalPrice,
                    stock: variant.stockQuantity || 0,
                    discount: bestDiscount,
                    discountBadge: renderDiscountBadge(bestDiscount),
                });
            });
        });

        return rows;
    }, [products]);

    const getStockStatus = (qty) => {
        if (qty > 10) return <span className="text-green-600 font-medium">Còn hàng</span>;
        if (qty > 0) return <span className="text-yellow-600 font-medium">Sắp hết</span>;
        return <span className="text-red-600 font-medium">Hết hàng</span>;
    };

    return (
        <div className="flex min-h-screen bg-gray-100">
            <AdminSidebar />
            <div className="flex-1 p-6 mt-[50px]">
                <div className="flex items-center justify-between mb-6">
                    <h1 className="text-3xl font-bold text-slate-800">Quản lý sản phẩm & biến thể</h1>
                    <Link
                        to="/admin/product/addproduct"
                        className="bg-green-600 hover:bg-green-700 text-white px-5 py-3 rounded-lg font-medium transition"
                    >
                        + Thêm sản phẩm
                    </Link>
                </div>

                <div className="bg-white rounded-lg shadow overflow-hidden">
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">ID Biến thể</th>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Ảnh</th>
                                <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Sản phẩm (Màu)</th>
                                <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Giá gốc</th>
                                <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Giá bán</th>
                                <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">
                                    Khuyến mãi
                                </th>
                                <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Tồn kho</th>
                                <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Trạng thái</th>
                                <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 uppercase">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-gray-200">
                            {variantRows.length > 0 ? (
                                variantRows.map((row, index) => (
                                    <tr key={row.variantId || `product-${row.productId}-${index}`} className="hover:bg-gray-50">
                                        <td className="px-4 py-4 text-sm text-gray-600">
                                            {row.variantId || "-"}
                                        </td>
                                        <td className="px-4 py-4">
                                            <img
                                                src={row.imageUrl || "/placeholder.jpg"}
                                                alt={row.displayName}
                                                className="w-16 h-16 object-cover rounded border"
                                                onError={(e) => e.target.src = "/placeholder.jpg"}
                                            />
                                        </td>
                                        <td className="px-4 py-4 text-sm">
                                            <div className="font-medium text-gray-900">{row.displayName}</div>
                                            {row.color !== "-" && (
                                                <div className="text-xs text-gray-500">Màu: {row.color}</div>
                                            )}
                                            {row.price !== row.finalPrice && row.discountBadge && (
                                                <div className="mt-1">{row.discountBadge}</div>
                                            )}

                                        </td>
                                        <td className="px-4 py-4 text-center text-sm text-gray-600">
                                            {Number(row.price).toLocaleString("vi-VN")} ₫
                                        </td>
                                        <td className="px-4 py-4 text-center">
                                            <div className="flex flex-col items-center gap-1">
                                                <div className="font-bold text-lg text-blue-600">
                                                    {Number(row.finalPrice).toLocaleString("vi-VN")} ₫

                                                </div>

                                            </div>
                                        </td>
                                        <DiscountSelector
                                            row={row}
                                            discounts={discounts}
                                            onSuccess={() => dispatch(fetchProduct(null, `pageNumber=${pageNumber}&pageSize=5&sortBy=productId&sortOrder=asc`))}
                                        />
                                        <td className="px-4 py-4 text-center text-sm font-medium">
                                            {row.stock}
                                        </td>
                                        <td className="px-4 py-4 text-center text-sm">
                                            {getStockStatus(row.stock)}
                                        </td>
                                        <td className="px-4 py-4 text-center">
                                            <div className="flex justify-center gap-4">
                                                {/* Nút 1: Sửa toàn bộ sản phẩm */}
                                                <Link
                                                    to={`/admin/product/update/${row.productId}`}
                                                    className="text-blue-600 hover:text-blue-800 transition"
                                                    title="Sửa sản phẩm"
                                                >
                                                    <FaEdit size={20} />
                                                </Link>

                                                {/* Nút 2: Chỉ sửa variant này (nếu có)
                                                {row.variantId && (
                                                    <Link
                                                        to={`/admin/variant/quick-edit/${row.variantId}`}
                                                        className="text-emerald-600 hover:text-emerald-800 transition"
                                                        title="Sửa nhanh biến thể"
                                                    >
                                                        <FaEdit size={18} />
                                                    </Link>
                                                )} */}

                                                <button className="text-red-600 hover:text-red-800 transition">
                                                    <MdDeleteOutline size={22} />
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="8" className="px-6 py-12 text-center text-gray-500 text-lg">
                                        Không có sản phẩm nào để hiển thị.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>

                {/* Phân trang */}
                <div className="mt-6 flex flex-col items-center">
                    <p className="text-sm text-gray-600 mb-3">
                        Trang <strong>{page}</strong> / {totalPages} —
                        Tổng: <strong>{totalElements}</strong> sản phẩm,
                        Đang hiển thị: <strong>{variantRows.length}</strong> biến thể
                    </p>
                    <PaginationRounded numberofPage={totalPages > 0 ? totalPages : 1} />
                </div>
            </div>
        </div>
    );
};

export default AdminProductList;