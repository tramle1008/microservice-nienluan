const StatCard = ({ label, value }) => (
    <div style={{
        background: "#f9f9f9",
        padding: "20px",
        borderRadius: "12px",
        boxShadow: "0 0 10px rgba(0,0,0,0.05)",
        textAlign: "center"
    }}>
        <h2 style={{ fontSize: "24px", marginBottom: "10px" }}>{value}</h2>
        <p style={{ fontSize: "16px", color: "#555" }}>{label}</p>
    </div>
);

export default StatCard;