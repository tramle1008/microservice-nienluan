const InputField = ({
    label,
    id,
    type,
    errors,
    register,
    required,
    message,
    className,
    min,
    value,
    placeholder,
    disabled = false,
}) => {
    return (
        <div className="flex flex-col gap-1 w-full">
            <label htmlFor={id} className={`${className || ""} font-bold text-sm text-slate-800`}>
                {label}
            </label>
            <input
                id={id}
                type={type}
                placeholder={placeholder}
                defaultValue={value}
                disabled={disabled}
                className={`${className || ""} px-2 py-2 border outline-none bg-transparent text-slate-800 rounded-md ${errors[id]?.message ? "border-red-500" : "border-slate-700"
                    }`}
                {...register(id, {
                    required: required ? { value: true, message } : false,
                    ...(min && { minLength: { value: min, message: `Ít nhất ${min} ký tự` } }),
                    ...(type === "email" && {
                        pattern: {
                            value: /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/,
                            message: "Email không hợp lệ",
                        },
                    }),
                    ...(type === "url" && {
                        pattern: {
                            value: /^(https?:\/\/)?(([a-zA-Z0-9\u00a1-\uffff-]+\.)+[a-zA-Z\u00a1-\uffff]{2,})(:\d{2,5})?(\/[^\s]*)?$/,
                            message: "Vui lòng nhập đúng định dạng URL",
                        },
                    }),
                })}
                min={type === "number" ? 0 : undefined}
            />
            {errors[id]?.message && (
                <p className="text-rose-500 text-sm">{errors[id]?.message}</p>
            )}
        </div>
    );
};

export default InputField;
