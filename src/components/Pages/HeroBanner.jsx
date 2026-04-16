// Import Swiper React components
import { Swiper, SwiperSlide } from 'swiper/react';

// Import Swiper styles
import 'swiper/css';
// Import Swiper styles
import 'swiper/css';
import 'swiper/css/pagination';
import 'swiper/css/navigation';
import 'swiper/css/effect-fade';
import 'swiper/css/autoplay';

import { Autoplay, EffectFade, Navigation, Pagination } from "swiper/modules";
import { bannerList } from '../../utils/dataTemp';
import { Link } from 'react-router-dom';


const colors = [
    "bg-[#6a11cb]",
    "bg-[#ff416c]",
    "bg-[#56ab2f]",
    "bg-[#2193b0]"
]
const HeroBanner = () => {
    return (

        <div className="py-2 rounded-md">
            <Swiper
                grabCursor={true}
                autoplay={{
                    delay: 4000,
                    disableOnInteraction: false,
                }}
                navigation
                modules={[Pagination, EffectFade, Navigation, Autoplay]}
                pagination={{ clickable: true }}
                scrollbar={{ draggable: true }}
                slidesPerView={1}
            >
                {bannerList.map((item, i) => (
                    <SwiperSlide key={item.id}>
                        <div className={`carousel-item rounded-md sm:h-[500px] h-96 ${colors[i]}`}>
                            <Link
                                to={`/products/tree?categoryId=${item.id}`}
                                className=""
                            >
                                {/* Flex container chính: chia 2 cột */}
                                <div className="flex flex-col lg:flex-row items-center h-full">

                                    {/* Text bên trái */}
                                    <div className="w-full lg:w-1/2 p-8 flex flex-col justify-center text-center lg:text-left lg:m-25">
                                        <h1 className="text-5xl text-white font-bold">
                                            {item.title}
                                        </h1>
                                        <p className="text-white mt-2">
                                            {item.description}
                                        </p>

                                    </div>

                                    {/* Ảnh bên phải */}
                                    <div className="w-full lg:w-1/2 flex justify-center p-4">
                                        <img
                                            src={item?.image}
                                            alt={item?.title}
                                            className="max-h-[400px] rounded-md"
                                        />
                                    </div>
                                </div>
                            </Link>
                        </div>
                    </SwiperSlide>
                ))}
            </Swiper>
        </div>
    )
}

export default HeroBanner;
