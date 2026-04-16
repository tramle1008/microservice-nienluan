import { Link } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import Navbar from "./Navbar";
import { useEffect } from "react";
import ProductCard from "./Product/ProductCard";
import HeroBanner from "./HeroBanner";
import DiscountSlider from "./Product/DiscountSlider";

const Home = () => {
    const dispatch = useDispatch();


    return (
        <>
            <div>
                <HeroBanner />
            </div>
            <section className="bg-[#92a695] pt-5 pb-10 px-4 sm:px-8 lg:px-14">
                {/* Danh mục Sản Phẩm Sale */}
                <DiscountSlider />
                {/* Danh mục  */}
            </section>
        </>
    );
};

export default Home;
