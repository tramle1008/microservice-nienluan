import { MenuItem, FormControl, InputLabel, Select, Button } from '@mui/material';
import { useEffect, useState } from "react";
import { FaSearch } from "react-icons/fa";
import { FiArrowDown, FiArrowUp } from "react-icons/fi";
import { useSearchParams } from "react-router-dom";

const ReusableFilter = ({
    filterParam = "categoryId",
    sortEnabled = true,
}) => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [sortOrder, setSortOrder] = useState("asc");
    const [searchText, setSearchText] = useState("");

    // Sync URL params with state
    useEffect(() => {
        const sort = searchParams.get("sortOrder") || "asc";
        const key = searchParams.get("key") || "";
        setSortOrder(sort);
        setSearchText(key);
    }, [searchParams, filterParam]);



    const toggleSort = () => {
        const newOrder = sortOrder === "asc" ? "desc" : "asc";
        const newParams = new URLSearchParams(searchParams);
        newParams.set("sortOrder", newOrder);
        setSortOrder(newOrder);
        setSearchParams(newParams);
    };



    const handleClearFilter = () => {
        const cleared = new URLSearchParams();
        setSearchParams(cleared);
    };


    return (
        <div className="flex lg:flex-row flex-col-reverse lg:justify-between gap-4">
            <div className="flex flex-col lg:flex-row gap-2 items-center px-4">
                {sortEnabled && (
                    <Button variant="outlined" color="success" onClick={toggleSort}>
                        Sort {sortOrder === "asc" ? <FiArrowUp /> : <FiArrowDown />}
                    </Button>
                )}

                <Button variant="outlined" color="error" onClick={handleClearFilter}>
                    Clear
                </Button>
            </div>
        </div>
    );
};
export default ReusableFilter;