import { Button, Menu, MenuItem, Typography } from "@mui/material";
import React, { use } from "react";
import { useDispatch, useSelector } from "react-redux";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import toast from "react-hot-toast";
const UserMenu = () => {
    const [anchorEl, setAnchorEl] = React.useState(null);
    const { user } = useSelector((state) => state.auth);
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const open = Boolean(anchorEl);
    const handleClick = (event) => {
        setAnchorEl(event.currentTarget);
    };
    const handleClose = () => {
        setAnchorEl(null);
    };

    const handleProfileClick = () => {
        handleClose();
        navigate("/user/profile"); // <-- Điều hướng đến trang hồ sơ
    };
    const handleOrderClick = () => {
        handleClose();
        navigate("/user/order"); // <-- Điều hướng đến trang hồ sơ
    };

    // Khi nhấn Logout
    const handleLogout = () => {
        localStorage.removeItem("auth"); // hoặc tên token bạn lưu
        window.location.href = "/login";
    };

    return (
        <div>
            <Button
                id="basic-button"
                aria-controls={open ? 'basic-menu' : undefined}
                aria-haspopup="true"
                aria-expanded={open ? 'true' : undefined}
                onClick={handleClick}
            >
                <div className="text-sm font-semibold text-[#1a1a1a] border border-[#d9a05b] px-3 py-1 rounded-md bg-white shadow-sm">
                    {user?.userName}
                </div>


            </Button>
            <Menu
                id="basic-menu"
                anchorEl={anchorEl}
                open={open}
                onClose={handleClose}
                slotProps={{
                    list: {
                        'aria-labelledby': 'basic-button',
                    },
                }}
            >
                <MenuItem onClick={handleProfileClick}>Hồ Sơ</MenuItem>
                <MenuItem onClick={handleOrderClick}>Đơn Hàng</MenuItem>
                <MenuItem onClick={handleLogout}>Logout</MenuItem>
            </Menu>
        </div>
    );
}

export default UserMenu;