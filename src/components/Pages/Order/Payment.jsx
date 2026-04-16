import { useEffect, useState } from "react";
import axios from "axios";
import { Box, Button, Typography } from "@mui/material";
import { toast } from "react-hot-toast";
import { useDispatch, useSelector } from "react-redux";
import ItemContent from "../Cart/ItemContent";
import api from "../../../api/api";
import { checkShippingFeeByAddress } from "../../../store/actions";
import CountdownTimer from "./CountdownTimer";

const Payment = ({ onNext, onBack, addressId, paymentMethod }) => {
    const dispatch = useDispatch();
    const [qrUrl, setQrUrl] = useState(null);
    const [transactionCode, setTransactionCode] = useState(null);
    const [amount, setAmount] = useState(null);
    const [orderCode, setOrderCode] = useState(null);
    const [checkingStatus, setCheckingStatus] = useState(false);
    const { loading, items, totalPrice, error } = useSelector((state) => state.cart);
    const { loadingA, fee, errorA } = useSelector(state => state.shipping);

    const auth = localStorage.getItem("auth");
    if (!auth) {
        toast.error("Bạn cần đăng nhập");
        return null;
    }

    useEffect(() => {
        if (addressId) {
            dispatch(checkShippingFeeByAddress(addressId));
        }
    }, [addressId]);

    const shippingFee = fee
        ? (totalPrice >= Number(fee.freeShippingThreshold)
            ? 0
            : Number(fee.defaultFee))
        : null;
    const grandTotal = Number(totalPrice) + Number(shippingFee);
    const token = JSON.parse(auth).jwtToken;

    const handlePlaceOrder = async () => {
        if (paymentMethod === "COD") {
            try {
                const res = await api.post(
                    "/orders",
                    {
                        addressId: addressId,        // Đảm bảo gửi addressId
                        paymentMethod: "COD"         // BẮT BUỘC gửi paymentMethod
                    },
                    {
                        headers: { Authorization: `Bearer ${token}` },
                    }
                );
                onNext();
            } catch (err) {
                const message =
                    err.response?.data || "Đặt hàng thất bại";

                toast.error(message);
                console.error(err);
            }

        } else if (paymentMethod === "QR") {
            try {
                const res = await api.post(
                    "/orders",
                    {
                        addressId: addressId,        // Đảm bảo gửi addressId
                        paymentMethod: "QR"         // BẮT BUỘC gửi paymentMethod
                    },
                    { headers: { Authorization: `Bearer ${token}` } }
                );
                setQrUrl(res.data.qrPayment.qrUrl);
                setTransactionCode(res.data.qrPayment.transactionCode);
                setAmount(res.data.qrPayment.amount);
                setOrderCode(res.data.qrPayment.transactionCode); // DH1765174135884

                setCheckingStatus(true); //  Kích hoạt useEffect kiểm tra

            } catch (err) {
                console.error("Lỗi tạo QR:", err);
                toast.error("Không thể tạo QR thanh toán");
            }
        }
    };

    const checkPaymentStatus = async () => {
        if (!orderCode) return;

        try {
            const res = await api.get(
                `/payments/status/${orderCode}`,
                {
                    headers: { Authorization: `Bearer ${token}` },
                }
            );

            if (res.data === "PAID") {
                toast.success("Đã xác nhận thanh toán thành công!");
                setCheckingStatus(false);
                onNext();
            } else {
                console.log("Chưa thanh toán...");
            }
        } catch (err) {
            console.error("Lỗi kiểm tra trạng thái:", err);
        }
    };

    useEffect(() => {
        if (!orderCode || checkingStatus === false) return;

        const interval = setInterval(() => {
            checkPaymentStatus();
        }, 7000);

        // Dừng sau 5 phút
        const timeout = setTimeout(() => {
            toast.error("Hết thời gian chờ thanh toán. Vui lòng thử lại.");
            setCheckingStatus(false);
        }, 300000); // 5 phút = 300.000 ms

        return () => {
            clearInterval(interval);
            clearTimeout(timeout);
        };
    }, [orderCode, checkingStatus]);

    return (
        <div className=" flex justify-center text-center">
            <Box>
                {!qrUrl && (
                    <>
                        <Typography variant="h6" gutterBottom>Đơn Hàng</Typography>
                        <div className="grid grid-cols-5 bg-[#7f9c8f] p-1.5 mb-0.5 gap-7">
                            <p>Hình ảnh</p>
                            <p>Tên</p>
                            <p>Màu</p>
                            <p>Số lượng</p>
                            <p>Giá</p>
                        </div>
                        <div className="grid grid-cols-1 gap-5">
                            {items.map(item => (
                                <div key={item.itemId} className="grid grid-cols-1 gap-5">
                                    <div className="grid grid-cols-5 items-center">
                                        <img
                                            src={item.imageUrl}
                                            alt={item.productName}
                                            className="w-28 h-28 object-cover rounded-md"
                                        />
                                        <div>
                                            <h4 className="font-semibold">{item.productName}</h4>
                                        </div>
                                        <div>
                                            <h4 className="font-semibold">{item.color}</h4>
                                        </div>
                                        <div>
                                            <h4 className="font-semibold">{item.quantity?.toLocaleString()}</h4>
                                        </div>
                                        <div>
                                            <h4 className="font-semibold">{item.subtotal?.toLocaleString()}đ</h4>
                                        </div>

                                    </div>
                                </div>
                            ))}
                            <div className="mt-4 rounded-xl bg-gray-50 p-4 shadow-sm border border-gray-200">
                                <ul className="space-y-2 text-lg font-medium text-gray-800">
                                    {fee ? (
                                        <>
                                            <li className="flex justify-between">
                                                <span>Địa chỉ:</span>
                                                <span className="font-semibold">
                                                    {fee.detail}, {fee.ward}, {fee.province} - sđt: {fee.phoneNumber}
                                                </span>
                                            </li>

                                            {/* <li className="text-sm text-blue-400 font-medium">
                                                Miễn phí vận chuyển đơn từ {Number(fee.freeShippingThreshold).toLocaleString()} đ
                                            </li> */}
                                        </>
                                    ) : (
                                        <li className="text-sm text-gray-500 italic">Đang tính phí vận chuyển...</li>
                                    )}
                                    {fee ? (
                                        <>
                                            <li className="flex justify-between">
                                                <span>Phí vận chuyển:</span>
                                                <span className="font-semibold">
                                                    {Number(shippingFee).toLocaleString()} đ
                                                </span>
                                            </li>

                                            <li className="text-sm text-green-800 font-medium">
                                                Miễn phí vận chuyển đơn từ {Number(fee.freeShippingThreshold).toLocaleString()} đ
                                            </li>
                                        </>
                                    ) : (
                                        <li className="text-sm text-gray-500 italic">Đang tính phí vận chuyển...</li>
                                    )}

                                    <li className="flex justify-between pt-2 border-t border-gray-200 text-xl">
                                        <span>Tổng cộng:</span>
                                        <span className="font-bold text-red-500">
                                            {Number(grandTotal).toLocaleString()} đ
                                        </span>
                                    </li>
                                </ul>
                            </div>

                        </div>
                    </>
                )}

                {paymentMethod === "COD" && (
                    <div className="mt-4 mb-10">
                        <Typography>Bạn sẽ thanh toán khi nhận hàng.</Typography>
                        <div className="flex gap-4 justify-center mt-6">
                            <button
                                onClick={onBack}
                                className="text-rose-700 border border-rose-600 rounded-md bg-red-100 hover:bg-rose-600 hover:text-white transition-colors duration-200 px-6 py-2 font-semibold"
                            >
                                Quay lại
                            </button>

                            <button
                                onClick={handlePlaceOrder}
                                className="text-blue-700 border border-blue-600 rounded-md bg-blue-100 hover:bg-blue-600 hover:text-white transition-colors duration-200 px-6 py-2 font-semibold"
                            >
                                Xác nhận đơn hàng
                            </button>
                        </div>

                    </div>
                )}

                {paymentMethod === "QR" && (
                    <div className="mt-6 mb-10">
                        {!qrUrl ? (
                            // ... phần chưa tạo QR (giữ nguyên như cũ)
                            <div className="flex flex-col items-center gap-4">
                                <Typography>Nhấn để tạo mã QR thanh toán:</Typography>
                                <div className="flex gap-4 justify-center mt-6">
                                    <button
                                        onClick={onBack}
                                        className="text-rose-700 border border-rose-600 rounded-md bg-red-100 hover:bg-rose-600 hover:text-white transition-colors duration-200 px-6 py-2 font-semibold"
                                    >
                                        Quay lại
                                    </button>
                                    <Button variant="contained" color="primary" onClick={handlePlaceOrder}>
                                        Tạo mã QR
                                    </Button>
                                </div>
                            </div>
                        ) : (
                            <div className="text-center">
                                <Typography variant="h6" gutterBottom>
                                    Quét mã QR để thanh toán
                                </Typography>

                                {/* Đồng hồ đếm ngược */}


                                <div className="mt-8 max-w-5xl mx-auto px-4">

                                    <div className="flex justify-center mb-8">
                                        <CountdownTimer
                                            initialTime={300}
                                            onTimeout={() => {
                                                setCheckingStatus(false);
                                                toast.error("Hết thời gian thanh toán!");
                                            }}
                                        />
                                    </div>

                                    {/* 2. Thông tin + QR nằm ngang (mobile sẽ tự xuống dọc) */}
                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8 items-start">

                                        {/* Cột trái: Thông tin chuyển khoản */}
                                        <Box
                                            sx={{
                                                backgroundColor: "#f9fafb",
                                                border: "1px solid #e5e7eb",
                                                borderRadius: 3,
                                                p: 4,
                                                boxShadow: 2,
                                                height: "fit-content",
                                            }}
                                        >
                                            <Typography variant="h6" gutterBottom textAlign="center" color="primary">
                                                Thông tin chuyển khoản
                                            </Typography>
                                            <div className="space-y-4 mt-4 text-gray-800">
                                                <div className="flex justify-between">
                                                    <strong>Ngân hàng:</strong>
                                                    <span className="font-medium">VietinBank</span>
                                                </div>
                                                <div className="flex justify-between">
                                                    <strong>Chủ tài khoản:</strong>
                                                    <span className="font-medium">LE BICH TRAM</span>
                                                </div>
                                                <div className="flex justify-between">
                                                    <strong>Số tài khoản:</strong>
                                                    <span className="font-mono font-bold">108876614804</span>
                                                </div>
                                                <div className="pt-3 border-t border-gray-300">
                                                    <strong>Nội dung chuyển khoản:</strong>
                                                    <Typography
                                                        variant="body1"
                                                        className="font-mono text-red-600 text-lg break-all mt-2 bg-red-50 px-3 py-2 rounded-lg inline-block"
                                                    >
                                                        SEVQR{transactionCode}
                                                    </Typography>
                                                </div>
                                                <div className="pt-3 border-t border-gray-300 text-right">
                                                    <Typography variant="h5" className="text-green-600 font-bold">
                                                        {Number(amount || grandTotal).toLocaleString()} VNĐ
                                                    </Typography>
                                                </div>
                                            </div>
                                        </Box>

                                        {/* Cột phải: Mã QR */}
                                        <div className="flex justify-center">
                                            <img
                                                src={qrUrl}
                                                alt="QR thanh toán"
                                                className="w-72 h-72 md:w-80 md:h-80 rounded-2xl border-8 border-gray-200 shadow-2xl"
                                            />
                                        </div>
                                    </div>

                                    {/* Thông báo kiểm tra tự động */}
                                    <Typography variant="body2" color="warning.main" align="center" sx={{ mt: 4 }}>
                                        Đang tự động kiểm tra thanh toán mỗi 7 giây...
                                    </Typography>

                                    {/* Thông báo hết giờ */}
                                    {!checkingStatus && (
                                        <Typography color="error" align="center" sx={{ mt: 3, fontWeight: "bold", fontSize: "1.1rem" }}>
                                            Thanh toán chưa thành công sau 5 phút. Vui lòng thử lại hoặc đặt lại đơn hàng.
                                        </Typography>
                                    )}
                                </div>




                            </div>
                        )}
                    </div>
                )}

            </Box>
        </div>
    );
};

export default Payment;
