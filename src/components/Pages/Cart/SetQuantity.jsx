import { useEffect, useState } from "react";
import axios from "axios";
import toast from "react-hot-toast";
import api from "../../../api/api";
import { useDispatch } from "react-redux";
import { decrementItem, incrementItem } from "../../../store/actions";

const SetQuantity = ({ itemId, quantity, onUpdate }) => {
    const dispatch = useDispatch();

    const handleIncrement = () => {
        dispatch(incrementItem(itemId)).then(() => onUpdate());
    };

    const handleDecrement = () => {
        if (quantity > 1) {
            dispatch(decrementItem(itemId)).then(() => onUpdate());
        }
    };

    return (
        <div className="flex items-center border rounded">
            <button onClick={handleDecrement} className="px-2">-</button>
            <span className="px-3">{quantity}</span>              {/* tôi muốn chỗ này là input */}
            <button onClick={handleIncrement} className="px-2">+</button>
        </div>
    );
};


export default SetQuantity;
