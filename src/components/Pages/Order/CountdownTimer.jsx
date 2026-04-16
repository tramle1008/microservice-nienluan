// src/components/CountdownTimer.jsx
import { Box } from "@mui/material";
import { useEffect, useState } from "react";

const CountdownTimer = ({ initialTime = 300, onTimeout }) => {
    const [seconds, setSeconds] = useState(initialTime);

    useEffect(() => {
        // Reset lại thời gian khi initialTime thay đổi (trường hợp tạo QR mới)
        setSeconds(initialTime);
    }, [initialTime]);

    useEffect(() => {
        if (seconds <= 0) {
            onTimeout?.();
            return;
        }

        const timer = setInterval(() => {
            setSeconds((prev) => prev - 1);
        }, 1000);

        return () => clearInterval(timer);
    }, [seconds, onTimeout]);

    const formatTime = (secs) => {
        const mins = Math.floor(secs / 60);
        const secsRemain = secs % 60;
        return `${mins.toString().padStart(2, "0")}:${secsRemain
            .toString()
            .padStart(2, "0")}`;
    };

    return (
        <Box
            sx={{
                bgcolor: seconds <= 60 ? "#d32f2f" : "#009688",
                color: "white",
                px: 1.5,
                py: 1,
                borderRadius: 3,
                fontWeight: "bold",
                fontSize: "14px",
                display: "inline-block",
                boxShadow: 3,
                userSelect: "none",

                // Tạo viền kiểu outlined giống Button
                border: 2,
                borderColor: seconds <= 60 ? "#b71c1c" : "#00796b",
                // Nếu muốn viền trắng đẹp hơn khi nền xanh:
                // borderColor: seconds <= 60 ? "#b71c1c" : "white",
            }}
        >
            Thời gian còn lại: {formatTime(seconds)}


            <style jsx>{`
        @keyframes pulse {
          0% {
            transform: scale(1);
          }
          50% {
            transform: scale(1.05);
          }
          100% {
            transform: scale(1);
          }
        }
      `}</style>
        </Box>
    );
};

export default CountdownTimer;