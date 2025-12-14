import React, { useState, useEffect } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";

const API_BASE_URL = "http://localhost:8081/api";

const buttonAzulStyle = {
    background: "#007bff",
    color: "white",
    border: "none",
    borderRadius: "6px",
    padding: "10px 20px",
    fontWeight: "bold",
    fontSize: "15px",
    cursor: "pointer",
    transition: "background 0.2s"
};

const NoticiaDetalhes = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [noticia, setNoticia] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        fetchNoticia();
        // eslint-disable-next-line
    }, [id]);

    const fetchNoticia = async () => {
        try {
            const response = await axios.get(`${API_BASE_URL}/public/artigos/${id}`);
            setNoticia(response.data);
        } catch (err) {
            console.error("Erro ao carregar notícia:", err);
            alert("Erro ao carregar notícia ou notícia não encontrada.");
            navigate("/");
        } finally {
            setLoading(false);
        }
    };

    const avaliarNoticia = async (tipo) => {
        try {
            await axios.post(`${API_BASE_URL}/artigos/${id}/avaliar?avaliacao=${tipo}`);
            alert("Avaliação registrada!");
        } catch {
            alert("Ops! É necessário estar logado para avaliar.");
        }
    };

    if (loading) {
        return (
            <div style={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center"
            }}>
                <p>Carregando...</p>
            </div>
        );
    }

    if (!noticia) {
        return (
            <div style={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                flexDirection: "column",
                color: "#c00"
            }}>
                <p>Notícia não encontrada ou removida.</p>
                <button onClick={() => navigate("/")} style={{ ...buttonAzulStyle, marginTop: 24 }}>Voltar para Home</button>
            </div>
        );
    }

    return (
        <div style={{ minHeight: "100vh", backgroundColor: "#f5f5f5", display: "flex", flexDirection: "column" }}>
            <header style={{
                backgroundColor: "#1a1a1a",
                color: "white",
                padding: "20px 40px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center"
            }}>
                <h1 style={{ margin: 0, fontSize: "24px", fontWeight: "600" }}>
                    The Club
                </h1>
                <button
                    onClick={() => navigate("/")}
                    style={{
                        padding: "10px 20px",
                        background: "transparent",
                        color: "white",
                        border: "1px solid white",
                        borderRadius: "6px",
                        cursor: "pointer",
                        fontSize: "14px"
                    }}
                >
                    ← Voltar
                </button>
            </header>

            <main style={{ flex: 1, maxWidth: "800px", margin: "40px auto", padding: "0 20px" }}>
                <article style={{
                    backgroundColor: "white",
                    borderRadius: "12px",
                    padding: "40px",
                    boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
                }}>
                    {noticia.categoriaNome && (
                        <span style={{
                            display: "inline-block",
                            padding: "4px 12px",
                            backgroundColor: "#007bff",
                            color: "white",
                            borderRadius: "12px",
                            fontSize: "12px",
                            fontWeight: "500",
                            marginBottom: "16px"
                        }}>
                            {noticia.categoriaNome}
                        </span>
                    )}
                    <h1 style={{
                        margin: "0 0 16px 0",
                        fontSize: "32px",
                        fontWeight: "700",
                        lineHeight: "1.3",
                        color: "#1a1a1a"
                    }}>
                        {noticia.titulo}
                    </h1>
                    <div style={{
                        display: "flex",
                        gap: "16px",
                        marginBottom: "24px",
                        fontSize: "14px",
                        color: "#666"
                    }}>
                        {noticia.autorNome && <span>Por {noticia.autorNome}</span>}
                        {noticia.dataPublicacao && (
                            <span>
                                {new Date(noticia.dataPublicacao).toLocaleDateString("pt-BR")}
                            </span>
                        )}
                        {typeof noticia.visualizacoes === "number" && (
                            <span>
                                👁️ {noticia.visualizacoes} visualizações
                            </span>
                        )}
                    </div>
                    {noticia.imagemCapa && (
                        <div style={{
                            marginBottom: "32px",
                            borderRadius: "8px",
                            overflow: "hidden"
                        }}>
                            <img
                                src={noticia.imagemCapa}
                                alt={noticia.titulo}
                                style={{
                                    width: "100%",
                                    height: "auto",
                                    maxHeight: "500px",
                                    objectFit: "cover",
                                    objectPosition: "center",
                                    display: "block"
                                }}
                            />
                        </div>
                    )}
                    {noticia.resumo && (
                        <p style={{
                            fontSize: "18px",
                            lineHeight: "1.8",
                            color: "#333",
                            fontWeight: "500",
                            marginBottom: "24px",
                            paddingLeft: "16px",
                            borderLeft: "4px solid #007bff"
                        }}>
                            {noticia.resumo}
                        </p>
                    )}
                    <div style={{
                        fontSize: "16px",
                        lineHeight: "1.8",
                        color: "#333",
                        whiteSpace: "pre-wrap"
                    }}>
                        {noticia.conteudo}
                    </div>

                    {/* Avaliação da notícia */}
                    <div style={{
                        display: "flex",
                        gap: "16px",
                        alignItems: "center",
                        marginTop: "32px"
                    }}>
                        <strong>Avalie esta notícia:</strong>
                        <button
                            onClick={() => avaliarNoticia("GOSTEI")}
                            style={buttonAzulStyle}
                        >
                            Gostei
                        </button>
                        <button
                            onClick={() => avaliarNoticia("NEUTRO")}
                            style={buttonAzulStyle}
                        >
                            Neutro
                        </button>
                        <button
                            onClick={() => avaliarNoticia("NAO_GOSTEI")}
                            style={buttonAzulStyle}
                        >
                            Não Gostei
                        </button>
                    </div>
                </article>
            </main>

            <footer style={{
                backgroundColor: "#1a1a1a",
                color: "white",
                padding: "40px 20px 20px",
                marginTop: "60px",
                textAlign: "center"
            }}>
                <small>© 2025 The Club - Jornal Universitário | Todos os direitos reservados</small>
            </footer>
        </div>
    );
};

export default NoticiaDetalhes;
