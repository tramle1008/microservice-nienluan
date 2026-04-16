import { Box, Typography, Button } from "@mui/material";
import { useState } from "react";

const SelectMethod = ({ onNext, onBack, setPaymentMethod }) => {
    const [selectedMethod, setSelectedMethod] = useState("COD"); // mặc định là COD

    const handleNext = () => {
        setPaymentMethod(selectedMethod); // Gửi về Checkout
        onNext();
    };

    return (
        <div className="flex justify-center text-center mb-15">
            <Box className="w-full max-w-2xl mx-10">
                <Typography variant="h6" gutterBottom>Chọn phương thức thanh toán</Typography>

                <div className="grid grid-cols-1 gap-4 mx-24">
                    <label className="flex items-center gap-2">
                        <input
                            type="radio"
                            value="COD"
                            checked={selectedMethod === "COD"}
                            onChange={(e) => setSelectedMethod(e.target.value)}
                        />
                        Thanh toán khi nhận hàng (COD)
                    </label>

                    <label className="flex items-center gap-2">
                        <input
                            type="radio"
                            value="QR"
                            checked={selectedMethod === "QR"}
                            onChange={(e) => setSelectedMethod(e.target.value)}
                        />
                        Thanh toán trực tuyến
                    </label>
                </div>


                <Box mt={4}>
                    <div className="flex gap-4 justify-center mt-6">
                        <button onClick={onBack} className="text-rose-700 border border-rose-600 rounded-md bg-red-100 hover:bg-rose-600 hover:text-white transition-colors duration-200 px-6 py-2 font-semibold">Quay lại</button>
                        <button variant="contained" onClick={handleNext} className="text-blue-700 border border-blue-600 rounded-md bg-blue-100 hover:bg-blue-600 hover:text-white transition-colors duration-200 px-6 py-2 font-semibold">
                            Tiếp tục
                        </button>
                    </div>
                </Box>
            </Box>
        </div>

    );
};

export default SelectMethod;
