import React from 'react';
import { useNavigate } from "react-router-dom";
import { Box, Typography, Button } from '@mui/material';

const Complete = ({ onReset }) => {
    const navigate = useNavigate();
    return (
        <div className='my-10'>
            <Box textAlign="center" mt={5}>
                <Typography variant="h5" gutterBottom>
                    Đặt hàng thành công!
                </Typography>
                <Typography variant="body1" sx={{ mb: 3 }}>
                    Đơn hàng đã được ghi nhận. Chúng tôi sẽ nhanh chóng xác nhận và gửi hàng cho bạn.
                </Typography>
                <Typography>Bạn sẽ thanh toán khi nhận hàng.</Typography>
                <Button variant="contained" onClick={() => navigate("/")}>
                    Về trang sản phẩm
                </Button>
            </Box>
        </div>
    );
};

export default Complete;
