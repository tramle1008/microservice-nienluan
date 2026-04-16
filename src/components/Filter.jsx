import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { useDispatch, useSelector } from "react-redux";
import { FiArrowDown, FiArrowUp } from "react-icons/fi";
import { useEffect, useState } from "react";
import { FaSearch } from "react-icons/fa";
import { MenuItem, FormControl, InputLabel, Select, Button } from '@mui/material';
import { fetchCategories } from "../store/actions";

const Filter = () => {
    const dispatch = useDispatch();
    const navigate = useNavigate();
    const location = useLocation();

    const [searchParams, setSearchParams] = useSearchParams();

    const categories = useSelector((state) => state.products.categories);

    const [category, setCategory] = useState("all");
    const [sortOrder, setSortOrder] = useState("asc");
    const [searchItems, setSearchItems] = useState("");

    // Fetch categories
    useEffect(() => {
        dispatch(fetchCategories());
    }, [dispatch]);

    // Read params from URL
    useEffect(() => {
        const currentCategory = searchParams.get("categoryId") || "all";
        const currentSortOrder = searchParams.get("sortby") || "asc";
        const currentSearchItems = searchParams.get("key") || "";

        setCategory(currentCategory);
        setSortOrder(currentSortOrder);
        setSearchItems(currentSearchItems);
    }, [searchParams]);

    // Update category filter
    const handleCategoryChange = (e) => {
        const selectedValue = e.target.value;
        const newParams = new URLSearchParams(searchParams);

        if (selectedValue === "all") {
            newParams.delete("categoryId");
        } else {
            newParams.set("categoryId", selectedValue);
        }

        setCategory(selectedValue);
        setSearchParams(newParams);
    };

    // Toggle sort order
    const toggleSortOrder = () => {
        const newOrder = sortOrder === "asc" ? "desc" : "asc";
        const newParams = new URLSearchParams(searchParams);
        newParams.set("sortby", newOrder);
        setSortOrder(newOrder);
        setSearchParams(newParams);
    };

    // Clear filters
    const handleClearFilter = () => {
        setSearchParams({});
    };



    const handleSearchSubmit = () => {
        const newParams = new URLSearchParams(searchParams);
        if (searchItems.trim()) {
            newParams.set("key", searchItems.trim());
        } else {
            newParams.delete("key");
        }
        setSearchParams(newParams);
    };


    return (
        <div className="flex lg:flex-row flex-col-reverse lg:justify-between justify-center items-center gap-4">
            <div className="relative flex items-center 2xl:w-[450px] sm:w-[420px] w-full">
                <FaSearch className="absolute left-3 text-gray-500 cursor-pointer " onClick={handleSearchSubmit} />
                <input
                    type="text"
                    value={searchItems}
                    onChange={(c) => setSearchItems(c.target.value)}
                    onBlur={handleSearchSubmit}
                    onKeyDown={(e) => e.key === "Enter" && handleSearchSubmit()}
                    placeholder="Search products"
                    className="border border-gray-400 text-slate-800 bg-white/80 rounded-md py-2 pl-10 pr-4 w-full focus:outline-none focus:ring-2 focus:ring-[#469555]"
                />

            </div>

            <div className="flex flex-col lg:flex-row gap-2 items-center px-4 sm:px-8">
                <FormControl variant="outlined" size="small">
                    <InputLabel id="category-select-label">Category</InputLabel>
                    <Select
                        labelId="category-select-label"
                        value={category}
                        onChange={handleCategoryChange}
                        label="Category"
                    >
                        <MenuItem value="all">All</MenuItem>
                        {categories.map((i) => (
                            <MenuItem key={i.categoryID} value={i.categoryID.toString()}>
                                {i.categoryName}
                            </MenuItem>
                        ))}
                    </Select>
                </FormControl>

                <Button variant="outlined" color="success" onClick={toggleSortOrder}>
                    Sort by {sortOrder === "asc" ? <FiArrowUp /> : <FiArrowDown />}
                </Button>

                <Button variant="outlined" color="error" onClick={handleClearFilter}>
                    Clear
                </Button>
            </div>
        </div>
    );
};

export default Filter;
