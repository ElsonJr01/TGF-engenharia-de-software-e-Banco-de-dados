import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Editais = () => {
    const [editais, setEditais] = useState([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        fetchEditais();
    }, []);

    const fetchEditais = async () => {
        try {
            console.log("📄 Buscando editais...");
            const response = await axios.get("http://localhost:8081/api/public/editais");
            console.log("✅ Resposta:", response.data);

            const data = response.data.content || response.data || [];
            setEditais(Array.isArray(data) ? data : []);
            setLoading(false);
        } catch (err) {
            console.error("❌ Erro ao carregar editais:", err);
            setEditais([]);
            setLoading(false);
        }
    };

    const handleDownload = (edital) => {
        console.log("📥 Baixando:", edital.arquivoUrl);
        window.open(edital.arquivoUrl, '_blank');
    };

    if (loading) {
        return (
            <div style={{
                minHeight: "100vh",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                backgroundColor: "#f5f5f5"
            }}>
                <div style={{ textAlign: "center" }}>
                    <div style={{
                        width: "50px",
                        height: "50px",
                        border: "4px solid #f3f3f3",
                        borderTop: "4px solid #007bff",
                        borderRadius: "50%",
                        animation: "spin 1s linear infinite",
                        margin: "0 auto 16px"
                    }}></div>
                    <p style={{ fontSize: "16px", color: "#666" }}>Carregando editais...</p>
                    <style>{`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); }}`}</style>
                </div>
            </div>
        );
    }

    return (
        <div style={{ minHeight: "100vh", backgroundColor: "#f5f5f5" }}>
            {/* Barra Superior */}
            <div style={{
                backgroundColor: "#007bff",
                padding: "10px 0",
                boxShadow: "0 2px 4px rgba(0,0,0,0.1)"
            }}>
                <div style={{
                    maxWidth: "1200px",
                    margin: "0 auto",
                    padding: "0 20px",
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center"
                }}>
                    <span style={{ color: "white", fontSize: "14px" }}>
                        📄 Editais - The Club
                    </span>
                    <button
                        onClick={() => navigate("/")}
                        style={{
                            padding: "6px 16px",
                            backgroundColor: "white",
                            color: "#007bff",
                            border: "none",
                            borderRadius: "4px",
                            cursor: "pointer",
                            fontSize: "13px",
                            fontWeight: "600"
                        }}
                    >
                        🏠 Voltar
                    </button>
                </div>
            </div>

            {/* Header */}
            <header style={{
                backgroundColor: "white",
                borderBottom: "1px solid #e0e0e0",
                boxShadow: "0 2px 4px rgba(0,0,0,0.05)"
            }}>
                <div style={{
                    maxWidth: "1200px",
                    margin: "0 auto",
                    padding: "32px 20px",
                    textAlign: "center"
                }}>
                    <h1 style={{
                        margin: "0 0 12px 0",
                        fontSize: "42px",
                        fontWeight: "800",
                        color: "#1a1a1a"
                    }}>
                        📄 Editais
                    </h1>
                    <p style={{
                        margin: 0,
                        fontSize: "16px",
                        color: "#666"
                    }}>
                        Confira os editais publicados pela universidade
                    </p>
                </div>
            </header>

            {/* Conteúdo */}
            <main style={{ maxWidth: "1000px", margin: "40px auto", padding: "0 20px" }}>
                {editais.length > 0 ? (
                    <div style={{ display: "flex", flexDirection: "column", gap: "20px" }}>
                        {editais.map(edital => (
                            <div
                                key={edital.id}
                                style={{
                                    backgroundColor: "white",
                                    padding: "28px",
                                    borderRadius: "12px",
                                    boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                                    transition: "transform 0.2s, box-shadow 0.2s"
                                }}
                                onMouseEnter={(e) => {
                                    e.currentTarget.style.transform = "translateY(-4px)";
                                    e.currentTarget.style.boxShadow = "0 4px 16px rgba(0,0,0,0.15)";
                                }}
                                onMouseLeave={(e) => {
                                    e.currentTarget.style.transform = "translateY(0)";
                                    e.currentTarget.style.boxShadow = "0 2px 8px rgba(0,0,0,0.1)";
                                }}
                            >
                                <div style={{
                                    display: "flex",
                                    justifyContent: "space-between",
                                    alignItems: "flex-start",
                                    gap: "20px",
                                    flexWrap: "wrap"
                                }}>
                                    <div style={{ flex: 1, minWidth: "280px" }}>
                                        <h2 style={{
                                            margin: "0 0 16px 0",
                                            fontSize: "24px",
                                            fontWeight: "700",
                                            color: "#1a1a1a",
                                            lineHeight: "1.3"
                                        }}>
                                            {edital.titulo}
                                        </h2>

                                        {edital.descricao && (
                                            <p style={{
                                                margin: "0 0 20px 0",
                                                fontSize: "15px",
                                                color: "#555",
                                                lineHeight: "1.6"
                                            }}>
                                                {edital.descricao}
                                            </p>
                                        )}

                                        <div style={{
                                            display: "flex",
                                            flexWrap: "wrap",
                                            gap: "16px",
                                            marginBottom: "20px",
                                            fontSize: "14px",
                                            color: "#666"
                                        }}>
                                            <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                                                <span>📅</span>
                                                <span>Publicado: {new Date(edital.dataPublicacao).toLocaleDateString('pt-BR')}</span>
                                            </div>

                                            {edital.dataValidade && (
                                                <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                                                    <span>⏰</span>
                                                    <span>Válido até: {new Date(edital.dataValidade).toLocaleDateString('pt-BR')}</span>
                                                </div>
                                            )}

                                            <div style={{ display: "flex", alignItems: "center", gap: "6px" }}>
                                                <span>👁️</span>
                                                <span>{edital.visualizacoes || 0} visualizações</span>
                                            </div>
                                        </div>

                                        {edital.arquivoNome && (
                                            <div style={{
                                                display: "inline-flex",
                                                alignItems: "center",
                                                gap: "8px",
                                                padding: "8px 14px",
                                                backgroundColor: "#f8f9fa",
                                                borderRadius: "6px",
                                                fontSize: "13px",
                                                color: "#555",
                                                border: "1px solid #e9ecef"
                                            }}>
                                                <span>📎</span>
                                                <span>{edital.arquivoNome}</span>
                                            </div>
                                        )}
                                    </div>

                                    <button
                                        onClick={() => handleDownload(edital)}
                                        style={{
                                            padding: "14px 28px",
                                            backgroundColor: "#007bff",
                                            color: "white",
                                            border: "none",
                                            borderRadius: "8px",
                                            cursor: "pointer",
                                            fontSize: "16px",
                                            fontWeight: "600",
                                            whiteSpace: "nowrap",
                                            display: "flex",
                                            alignItems: "center",
                                            gap: "8px",
                                            transition: "background 0.2s, transform 0.2s"
                                        }}
                                        onMouseEnter={(e) => {
                                            e.target.style.backgroundColor = "#0056b3";
                                            e.target.style.transform = "scale(1.05)";
                                        }}
                                        onMouseLeave={(e) => {
                                            e.target.style.backgroundColor = "#007bff";
                                            e.target.style.transform = "scale(1)";
                                        }}
                                    >
                                        <span>📥</span>
                                        <span>Baixar PDF</span>
                                    </button>
                                </div>
                            </div>
                        ))}
                    </div>
                ) : (
                    <div style={{
                        backgroundColor: "white",
                        padding: "80px 40px",
                        borderRadius: "12px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                        textAlign: "center"
                    }}>
                        <div style={{ fontSize: "72px", marginBottom: "20px" }}>📄</div>
                        <h3 style={{
                            margin: "0 0 12px 0",
                            fontSize: "24px",
                            fontWeight: "600",
                            color: "#1a1a1a"
                        }}>
                            Nenhum edital disponível
                        </h3>
                        <p style={{
                            margin: 0,
                            fontSize: "16px",
                            color: "#666"
                        }}>
                            Quando novos editais forem publicados, eles aparecerão aqui.
                        </p>
                    </div>
                )}
            </main>

            {/* Footer */}
            <footer style={{
                backgroundColor: "#1a1a1a",
                color: "white",
                padding: "40px 20px",
                marginTop: "80px"
            }}>
                <div style={{
                    maxWidth: "1200px",
                    margin: "0 auto",
                    textAlign: "center"
                }}>
                    <p style={{
                        margin: "0 0 12px 0",
                        fontSize: "16px",
                        fontWeight: "600"
                    }}>
                        The Club - Jornal Universitário
                    </p>
                    <p style={{
                        margin: "0 0 20px 0",
                        fontSize: "14px",
                        color: "#999"
                    }}>
                        📧 clubedeprogramaçãounifesspa@gmail.com | 📱 (94) 99220-6286
                    </p>
                    <p style={{
                        margin: 0,
                        fontSize: "13px",
                        color: "#666"
                    }}>
                        © 2025 The Club. Todos os direitos reservados.
                    </p>
                </div>
            </footer>
        </div>
    );
};

export default Editais;
