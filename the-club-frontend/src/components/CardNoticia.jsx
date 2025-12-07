import React from "react";
import PropTypes from "prop-types";
import { useNavigate } from "react-router-dom";

const CardNoticia = ({ noticia }) => {
    const navigate = useNavigate();

    const handleVerMais = () => {
        navigate(`/noticia/${noticia.id}`);
    };

    return (
        <div
            style={{
                border: "1px solid #ddd",
                borderRadius: "8px",
                overflow: "hidden",
                boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                transition: "transform 0.2s",
                cursor: "pointer",
                height: "100%",
                display: "flex",
                flexDirection: "column"
            }}
            onMouseEnter={e => (e.currentTarget.style.transform = "translateY(-4px)")}
            onMouseLeave={e => (e.currentTarget.style.transform = "translateY(0)")}
            onClick={handleVerMais}
        >
            {/* Imagem */}
            {noticia.imagemCapa && (
                <div
                    style={{
                        width: "100%",
                        height: "200px",
                        overflow: "hidden",
                        backgroundColor: "#f0f0f0"
                    }}
                >
                    <img
                        src={noticia.imagemCapa}
                        alt={noticia.titulo}
                        style={{
                            width: "100%",
                            height: "100%",
                            objectFit: "cover",
                            objectPosition: "center"
                        }}
                    />
                </div>
            )}
            {/* Conteúdo */}
            <div
                style={{
                    padding: "16px",
                    flex: 1,
                    display: "flex",
                    flexDirection: "column"
                }}
            >
                <h3
                    style={{
                        margin: "0 0 12px 0",
                        fontSize: "18px",
                        fontWeight: "600",
                        lineHeight: "1.4"
                    }}
                >
                    {noticia.titulo}
                </h3>
                <p
                    style={{
                        margin: "0 0 16px 0",
                        color: "#666",
                        fontSize: "14px",
                        lineHeight: "1.6",
                        flex: 1
                    }}
                >
                    {noticia.resumo}
                </p>
                <button
                    onClick={handleVerMais}
                    style={{
                        padding: "8px 16px",
                        backgroundColor: "#007bff",
                        color: "white",
                        border: "none",
                        borderRadius: "6px",
                        cursor: "pointer",
                        fontSize: "14px",
                        fontWeight: "500",
                        alignSelf: "flex-start"
                    }}
                    onMouseEnter={e => (e.target.style.backgroundColor = "#0056b3")}
                    onMouseLeave={e => (e.target.style.backgroundColor = "#007bff")}
                >
                    Leia mais →
                </button>
            </div>
        </div>
    );
};

CardNoticia.propTypes = {
    noticia: PropTypes.shape({
        id: PropTypes.oneOfType([PropTypes.number, PropTypes.string]).isRequired,
        imagemCapa: PropTypes.string,
        titulo: PropTypes.string,
        resumo: PropTypes.string
    }).isRequired
};

export default CardNoticia;
