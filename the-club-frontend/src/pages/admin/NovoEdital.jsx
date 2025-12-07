import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const NovoEdital = () => {
    const [titulo, setTitulo] = useState("");
    const [descricao, setDescricao] = useState("");
    const [dataValidade, setDataValidade] = useState("");
    const [arquivo, setArquivo] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!arquivo || !titulo) {
            alert("Preencha título e arquivo!");
            return;
        }

        const token = localStorage.getItem("token");
        if (!token) {
            alert("Faça login!");
            navigate("/admin/login");
            return;
        }

        setLoading(true);

        try {
            console.log("📤 Enviando arquivo...");
            const formData = new FormData();
            formData.append("imagem", arquivo);

            const uploadResponse = await axios.post(
                "http://localhost:8081/api/upload",
                formData,
                {
                    headers: {
                        "Content-Type": "multipart/form-data",
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            console.log("✅ Upload concluído");

            const editalData = {
                titulo: titulo.trim(),
                descricao: descricao.trim() || null,
                arquivoUrl: uploadResponse.data.url,
                arquivoNome: arquivo.name,
                dataValidade: dataValidade ? new Date(dataValidade).toISOString() : null
            };

            console.log("📤 Criando edital...");
            await axios.post(
                "http://localhost:8081/api/editais",
                editalData,
                {
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            console.log("✅ Edital criado!");
            alert("✅ Edital publicado com sucesso!");
            navigate("/admin/dashboard");
        } catch (err) {
            console.error("❌ Erro:", err);
            alert("❌ Erro: " + (err.response?.data?.message || err.message));
        } finally {
            setLoading(false);
        }
    };

    return (
        <div style={{ minHeight: "100vh", backgroundColor: "#f5f5f5" }}>
            <header style={{
                backgroundColor: "#1a1a1a",
                color: "white",
                padding: "20px 40px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center"
            }}>
                <h1 style={{ margin: 0, fontSize: "24px" }}>📄 Novo Edital</h1>
                <button
                    onClick={() => navigate("/admin/dashboard")}
                    style={{
                        padding: "10px 20px",
                        backgroundColor: "#6c757d",
                        color: "white",
                        border: "none",
                        borderRadius: "6px",
                        cursor: "pointer"
                    }}
                >
                    ← Voltar
                </button>
            </header>

            <main style={{ maxWidth: "800px", margin: "40px auto", padding: "0 20px" }}>
                <form onSubmit={handleSubmit} style={{
                    backgroundColor: "white",
                    padding: "32px",
                    borderRadius: "12px",
                    boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
                }}>
                    <div style={{ marginBottom: "24px" }}>
                        <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                            Título do Edital *
                        </label>
                        <input
                            type="text"
                            value={titulo}
                            onChange={(e) => setTitulo(e.target.value)}
                            required
                            placeholder="Ex: Edital de Monitoria 2025"
                            style={{
                                width: "100%",
                                padding: "12px",
                                border: "2px solid #e0e0e0",
                                borderRadius: "8px",
                                fontSize: "14px"
                            }}
                        />
                    </div>

                    <div style={{ marginBottom: "24px" }}>
                        <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                            Descrição
                        </label>
                        <textarea
                            value={descricao}
                            onChange={(e) => setDescricao(e.target.value)}
                            placeholder="Descreva o edital..."
                            rows="4"
                            style={{
                                width: "100%",
                                padding: "12px",
                                border: "2px solid #e0e0e0",
                                borderRadius: "8px",
                                fontSize: "14px",
                                resize: "vertical",
                                fontFamily: "inherit"
                            }}
                        />
                    </div>

                    <div style={{ marginBottom: "24px" }}>
                        <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                            Data de Validade (opcional)
                        </label>
                        <input
                            type="date"
                            value={dataValidade}
                            onChange={(e) => setDataValidade(e.target.value)}
                            style={{
                                width: "100%",
                                padding: "12px",
                                border: "2px solid #e0e0e0",
                                borderRadius: "8px",
                                fontSize: "14px"
                            }}
                        />
                    </div>

                    <div style={{ marginBottom: "32px" }}>
                        <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                            Arquivo PDF *
                        </label>
                        <input
                            type="file"
                            accept=".pdf,.doc,.docx"
                            onChange={(e) => setArquivo(e.target.files[0])}
                            required
                            style={{
                                width: "100%",
                                padding: "12px",
                                border: "2px solid #e0e0e0",
                                borderRadius: "8px",
                                fontSize: "14px"
                            }}
                        />
                        {arquivo && (
                            <p style={{ marginTop: "8px", fontSize: "13px", color: "#28a745" }}>
                                ✅ {arquivo.name}
                            </p>
                        )}
                    </div>

                    <button
                        type="submit"
                        disabled={loading}
                        style={{
                            width: "100%",
                            padding: "14px",
                            backgroundColor: loading ? "#ccc" : "#28a745",
                            color: "white",
                            border: "none",
                            borderRadius: "8px",
                            fontSize: "16px",
                            fontWeight: "600",
                            cursor: loading ? "not-allowed" : "pointer"
                        }}
                    >
                        {loading ? "Publicando..." : "📤 Publicar Edital"}
                    </button>
                </form>
            </main>
        </div>
    );
};

export default NovoEdital;
