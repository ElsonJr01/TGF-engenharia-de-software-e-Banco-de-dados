import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const NovaNoticia = () => {
    const [titulo, setTitulo] = useState("");
    const [resumo, setResumo] = useState("");
    const [conteudo, setConteudo] = useState("");
    const [imagemCapa, setImagemCapa] = useState("");
    const [categoriaId, setCategoriaId] = useState("");
    const [categorias, setCategorias] = useState([]);
    const [loading, setLoading] = useState(false);
    const [uploadingImage, setUploadingImage] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchCategorias();
    }, []);

    const fetchCategorias = async () => {
        try {
            const response = await axios.get("http://localhost:8081/api/public/categorias");
            console.log("📋 Categorias recebidas:", response.data);
            setCategorias(response.data || []);

            if (response.data && response.data.length === 0) {
                alert("⚠️ Nenhuma categoria encontrada. Cadastre categorias primeiro!");
            }
        } catch (err) {
            console.error("❌ Erro ao buscar categorias:", err);
            alert("❌ Erro ao carregar categorias. Verifique o backend.");
        }
    };

    const handleImageUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setUploadingImage(true);
        const formData = new FormData();
        formData.append("imagem", file);

        try {
            console.log("📤 Enviando imagem:", file.name);
            const response = await axios.post("http://localhost:8081/api/upload", formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });
            console.log("✅ Upload OK:", response.data);
            setImagemCapa(response.data.url);
            alert("✅ Imagem enviada!");
        } catch (err) {
            console.error("❌ Erro upload:", err);
            alert("❌ Erro ao enviar imagem: " + (err.response?.data?.message || err.message));
        } finally {
            setUploadingImage(false);
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!categoriaId || categoriaId === "") {
            alert("❌ Selecione uma categoria!");
            return;
        }

        if (!titulo.trim()) {
            alert("❌ Título não pode estar vazio!");
            return;
        }

        setLoading(true);

        try {
            const token = localStorage.getItem("token");

            if (!token) {
                alert("❌ Você não está autenticado. Faça login novamente.");
                navigate("/admin/login");
                return;
            }

            const dados = {
                titulo: titulo.trim(),
                resumo: resumo.trim(),
                conteudo: conteudo.trim(),
                imagemCapa: imagemCapa || null,
                categoriaId: parseInt(categoriaId),
                status: "PUBLICADO"
            };

            if (!dados.categoriaId || isNaN(dados.categoriaId)) {
                alert("❌ ID da categoria inválido!");
                setLoading(false);
                return;
            }

            const response = await axios.post("http://localhost:8081/api/artigos", dados, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            console.log("✅ Artigo criado:", response.data);
            alert("✅ Notícia criada com sucesso!");
            navigate("/admin/dashboard");
        } catch (err) {
            console.error("❌ Erro completo:", err);
            console.error("❌ Resposta do servidor:", err.response?.data);
            console.error("❌ Status:", err.response?.status);

            const mensagem = err.response?.data?.message ||
                err.response?.data?.error ||
                err.message;

            alert("❌ Erro ao criar notícia: " + mensagem);
            setLoading(false);
        }
    };

    return (
        <div style={{ minHeight: "100vh", backgroundColor: "#f5f5f5" }}>
            <header style={{
                backgroundColor: "#1a1a1a", color: "white", padding: "20px 40px",
                display: "flex", justifyContent: "space-between", alignItems: "center"
            }}>
                <h1 style={{ margin: 0, fontSize: "24px" }}>✨ Nova Notícia</h1>
                <button onClick={() => navigate("/admin/dashboard")} style={{
                    padding: "10px 20px", background: "transparent", color: "white",
                    border: "1px solid white", borderRadius: "6px", cursor: "pointer"
                }}>← Voltar</button>
            </header>

            <main style={{ maxWidth: "800px", margin: "40px auto", padding: "0 20px" }}>
                <div style={{ background: "white", borderRadius: "12px", padding: "32px", boxShadow: "0 2px 8px rgba(0,0,0,0.1)" }}>
                    <form onSubmit={handleSubmit}>
                        <div style={{ marginBottom: "20px" }}>
                            <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>Título *</label>
                            <input type="text" value={titulo} onChange={(e) => setTitulo(e.target.value)} required
                                   style={{ width: "100%", padding: "12px", border: "2px solid #e0e0e0", borderRadius: "6px" }} />
                        </div>

                        <div style={{ marginBottom: "20px" }}>
                            <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>Resumo *</label>
                            <textarea value={resumo} onChange={(e) => setResumo(e.target.value)} required rows="3"
                                      style={{ width: "100%", padding: "12px", border: "2px solid #e0e0e0", borderRadius: "6px", fontFamily: "inherit" }} />
                        </div>

                        <div style={{ marginBottom: "20px" }}>
                            <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>Conteúdo *</label>
                            <textarea value={conteudo} onChange={(e) => setConteudo(e.target.value)} required rows="10"
                                      style={{ width: "100%", padding: "12px", border: "2px solid #e0e0e0", borderRadius: "6px", fontFamily: "inherit" }} />
                        </div>

                        <div style={{ marginBottom: "20px" }}>
                            <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                                Categoria *
                                {categorias.length === 0 && <span style={{ color: "red", fontSize: "12px", marginLeft: "8px" }}>(Carregando...)</span>}
                                {categorias.length > 0 && <span style={{ color: "green", fontSize: "12px", marginLeft: "8px" }}>({categorias.length} disponíveis)</span>}
                            </label>
                            <select value={categoriaId} onChange={(e) => setCategoriaId(e.target.value)} required
                                    style={{ width: "100%", padding: "12px", border: "2px solid #e0e0e0", borderRadius: "6px" }}>
                                <option value="">Selecione uma categoria</option>
                                {categorias.map(cat => (
                                    <option key={cat.id} value={cat.id}>{cat.nome}</option>
                                ))}
                            </select>
                        </div>

                        <div style={{ marginBottom: "20px" }}>
                            <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>Imagem de Capa</label>
                            <input type="file" accept="image/*" onChange={handleImageUpload} disabled={uploadingImage}
                                   style={{ width: "100%", padding: "12px", border: "2px solid #e0e0e0", borderRadius: "6px" }} />
                            {uploadingImage && <p style={{ color: "#007bff", marginTop: "8px" }}>📤 Enviando imagem...</p>}
                            {imagemCapa && (
                                <div style={{ marginTop: "12px" }}>
                                    <img src={imagemCapa} alt="Preview"
                                         style={{ maxWidth: "200px", borderRadius: "6px", border: "2px solid #28a745" }} />
                                    <p style={{ color: "#28a745", fontSize: "12px", marginTop: "4px" }}>✅ Imagem carregada</p>
                                </div>
                            )}
                        </div>

                        <button type="submit" disabled={loading || uploadingImage}
                                style={{
                                    width: "100%", padding: "14px",
                                    background: (loading || uploadingImage) ? "#ccc" : "#28a745",
                                    color: "white", border: "none", borderRadius: "6px",
                                    fontSize: "16px", fontWeight: "600",
                                    cursor: (loading || uploadingImage) ? "not-allowed" : "pointer"
                                }}>
                            {loading ? "Criando..." : uploadingImage ? "⏳ Aguarde upload..." : "✅ Criar Notícia"}
                        </button>
                    </form>
                </div>
            </main>
        </div>
    );
};

export default NovaNoticia;
