import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate, useParams } from "react-router-dom";

const EditarNoticia = () => {
    const { id } = useParams();
    const [titulo, setTitulo] = useState("");
    const [resumo, setResumo] = useState("");
    const [conteudo, setConteudo] = useState("");
    const [imagemCapa, setImagemCapa] = useState("");
    const [imagemAtual, setImagemAtual] = useState(""); // Para mostrar a imagem atual
    const [categoriaId, setCategoriaId] = useState("");
    const [status, setStatus] = useState("PUBLICADO");
    const [categorias, setCategorias] = useState([]);
    const [loading, setLoading] = useState(false);
    const [loadingData, setLoadingData] = useState(true);
    const [uploadingImage, setUploadingImage] = useState(false);
    const navigate = useNavigate();

    useEffect(() => {
        fetchCategorias();
        fetchArtigo();
    }, [id]);

    const fetchCategorias = async () => {
        try {
            const response = await axios.get("http://localhost:8081/api/public/categorias");
            setCategorias(response.data || []);
        } catch (err) {
            console.error("Erro ao buscar categorias:", err);
        }
    };

    const fetchArtigo = async () => {
        try {
            const response = await axios.get(`http://localhost:8081/api/public/artigos/${id}`);
            const art = response.data;
            console.log("Artigo carregado:", art);

            setTitulo(art.titulo);
            setResumo(art.resumo);
            setConteudo(art.conteudo);
            setImagemAtual(art.imagemCapa || "");
            setImagemCapa(art.imagemCapa || "");
            setCategoriaId(art.categoria?.id || "");
            setStatus(art.status || "PUBLICADO");
            setLoadingData(false);
        } catch (err) {
            console.error("Erro ao carregar artigo:", err);
            alert("Erro ao carregar artigo");
            navigate("/admin/dashboard");
        }
    };

    const handleImageUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setUploadingImage(true);
        const formData = new FormData();
        formData.append("imagem", file);

        try {
            const response = await axios.post("http://localhost:8081/api/upload", formData, {
                headers: { "Content-Type": "multipart/form-data" }
            });
            console.log("Resposta upload:", response.data);
            setImagemCapa(response.data.url);
            setImagemAtual(response.data.url);
            alert("Imagem enviada com sucesso!");
        } catch (err) {
            console.error("Erro ao enviar imagem:", err);
            alert("Erro ao enviar imagem");
        } finally {
            setUploadingImage(false);
        }
    };

    const handleRemoverImagem = () => {
        if (window.confirm("Deseja remover a imagem de capa?")) {
            setImagemCapa("");
            setImagemAtual("");
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const token = localStorage.getItem("token");

            const dados = {
                titulo: titulo,
                resumo: resumo,
                conteudo: conteudo,
                imagemCapa: imagemCapa || null,
                categoriaId: parseInt(categoriaId),
                status: status
            };

            console.log("Enviando dados:", dados);

            const response = await axios.put(`http://localhost:8081/api/artigos/${id}`, dados, {
                headers: {
                    'Authorization': `Bearer ${token}`,
                    'Content-Type': 'application/json'
                }
            });

            console.log("Resposta do servidor:", response.data);
            alert("Notícia atualizada com sucesso!");
            navigate("/admin/dashboard");
        } catch (err) {
            console.error("Erro completo:", err);
            console.error("Resposta do erro:", err.response?.data);

            const mensagemErro = err.response?.data?.message ||
                err.response?.data?.erro ||
                "Erro ao atualizar notícia";
            alert(mensagemErro);
            setLoading(false);
        }
    };

    if (loadingData) {
        return (
            <div style={{
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                minHeight: "100vh",
                flexDirection: "column"
            }}>
                <div style={{
                    width: "50px",
                    height: "50px",
                    border: "4px solid #f3f3f3",
                    borderTop: "4px solid #007bff",
                    borderRadius: "50%",
                    animation: "spin 1s linear infinite",
                    marginBottom: "16px"
                }}></div>
                <p>Carregando artigo...</p>
                <style>{`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); }}`}</style>
            </div>
        );
    }

    return (
        <div style={{ minHeight: "100vh", backgroundColor: "#f5f5f5" }}>
            {/* Header */}
            <header style={{
                backgroundColor: "#1a1a1a",
                color: "white",
                padding: "20px 40px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center"
            }}>
                <h1 style={{ margin: 0, fontSize: "24px" }}>✏️ Editar Notícia</h1>
                <button onClick={() => navigate("/admin/dashboard")} style={{
                    padding: "10px 20px",
                    background: "transparent",
                    color: "white",
                    border: "1px solid white",
                    borderRadius: "6px",
                    cursor: "pointer"
                }}>← Voltar</button>
            </header>

            {/* Formulário */}
            <main style={{ maxWidth: "800px", margin: "40px auto", padding: "0 20px" }}>
                <div style={{
                    background: "white",
                    borderRadius: "12px",
                    padding: "32px",
                    boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
                }}>
                    <form onSubmit={handleSubmit}>
                        <div style={{ marginBottom: "20px" }}>
                            <label style={{
                                display: "block",
                                marginBottom: "8px",
                                fontWeight: "500"
                            }}>Título</label>
                            <input
                                type="text"
                                value={titulo}
                                onChange={(e) => setTitulo(e.target.value)}
                                required
                                style={{
                                    width: "100%",
                                    padding: "12px",
                                    border: "2px solid #e0e0e0",
                                    borderRadius: "6px",
                                    fontSize: "14px"
                                }}
                            />
                        </div>

                        <div style={{ marginBottom: "20px" }}>
                            <label style={{
                                display: "block",
                                marginBottom: "8px",
                                fontWeight: "500"
                            }}>Resumo</label>
                            <textarea
                                value={resumo}
                                onChange={(e) => setResumo(e.target.value)}
                                required
                                rows="3"
                                style={{
                                    width: "100%",
                                    padding: "12px",
                                    border: "2px solid #e0e0e0",
                                    borderRadius: "6px",
                                    fontSize: "14px",
                                    fontFamily: "inherit"
                                }}
                            />
                        </div>

                        <div style={{ marginBottom: "20px" }}>
                            <label style={{
                                display: "block",
                                marginBottom: "8px",
                                fontWeight: "500"
                            }}>Conteúdo</label>
                            <textarea
                                value={conteudo}
                                onChange={(e) => setConteudo(e.target.value)}
                                required
                                rows="10"
                                style={{
                                    width: "100%",
                                    padding: "12px",
                                    border: "2px solid #e0e0e0",
                                    borderRadius: "6px",
                                    fontSize: "14px",
                                    fontFamily: "inherit"
                                }}
                            />
                        </div>

                        <div style={{ marginBottom: "20px" }}>
                            <label style={{
                                display: "block",
                                marginBottom: "8px",
                                fontWeight: "500"
                            }}>Categoria</label>
                            <select
                                value={categoriaId}
                                onChange={(e) => setCategoriaId(e.target.value)}
                                required
                                style={{
                                    width: "100%",
                                    padding: "12px",
                                    border: "2px solid #e0e0e0",
                                    borderRadius: "6px",
                                    fontSize: "14px"
                                }}
                            >
                                <option value="">Selecione uma categoria</option>
                                {categorias.map(cat => (
                                    <option key={cat.id} value={cat.id}>{cat.nome}</option>
                                ))}
                            </select>
                        </div>

                        <div style={{ marginBottom: "20px" }}>
                            <label style={{
                                display: "block",
                                marginBottom: "8px",
                                fontWeight: "500"
                            }}>Status</label>
                            <select
                                value={status}
                                onChange={(e) => setStatus(e.target.value)}
                                required
                                style={{
                                    width: "100%",
                                    padding: "12px",
                                    border: "2px solid #e0e0e0",
                                    borderRadius: "6px",
                                    fontSize: "14px"
                                }}
                            >
                                <option value="PUBLICADO">✅ Publicado</option>
                                <option value="RASCUNHO">📝 Rascunho</option>
                                <option value="REVISAO">🔍 Em Revisão</option>
                            </select>
                        </div>

                        <div style={{ marginBottom: "20px" }}>
                            <label style={{
                                display: "block",
                                marginBottom: "8px",
                                fontWeight: "500"
                            }}>Imagem de Capa</label>

                            {/* Imagem Atual */}
                            {imagemAtual && (
                                <div style={{ marginBottom: "12px", position: "relative", display: "inline-block" }}>
                                    <img
                                        src={imagemAtual}
                                        alt="Capa atual"
                                        style={{
                                            maxWidth: "300px",
                                            borderRadius: "8px",
                                            border: "2px solid #e0e0e0"
                                        }}
                                    />
                                    <button
                                        type="button"
                                        onClick={handleRemoverImagem}
                                        style={{
                                            position: "absolute",
                                            top: "8px",
                                            right: "8px",
                                            padding: "8px 12px",
                                            background: "#dc3545",
                                            color: "white",
                                            border: "none",
                                            borderRadius: "6px",
                                            cursor: "pointer",
                                            fontSize: "14px"
                                        }}
                                    >
                                        🗑️ Remover
                                    </button>
                                </div>
                            )}

                            {/* Upload Nova Imagem */}
                            <input
                                type="file"
                                accept="image/*"
                                onChange={handleImageUpload}
                                disabled={uploadingImage}
                                style={{
                                    width: "100%",
                                    padding: "12px",
                                    border: "2px solid #e0e0e0",
                                    borderRadius: "6px"
                                }}
                            />
                            {uploadingImage && <p style={{ color: "#007bff", marginTop: "8px" }}>Enviando imagem...</p>}
                        </div>

                        <button
                            type="submit"
                            disabled={loading || uploadingImage}
                            style={{
                                width: "100%",
                                padding: "14px",
                                background: (loading || uploadingImage) ? "#ccc" : "#007bff",
                                color: "white",
                                border: "none",
                                borderRadius: "6px",
                                fontSize: "16px",
                                fontWeight: "600",
                                cursor: (loading || uploadingImage) ? "not-allowed" : "pointer"
                            }}
                        >
                            {loading ? "Salvando..." : uploadingImage ? "Aguarde o upload..." : "💾 Salvar Alterações"}
                        </button>
                    </form>
                </div>
            </main>
        </div>
    );
};

export default EditarNoticia;
