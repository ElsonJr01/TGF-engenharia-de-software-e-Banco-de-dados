import React, { useState, useEffect } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import PropTypes from "prop-types";

const Dashboard = () => {
    const [noticias, setNoticias] = useState([]);
    const [editais, setEditais] = useState([]);
    const [categorias, setCategorias] = useState([]);
    const [loading, setLoading] = useState(true);
    const [mostrarFormEdital, setMostrarFormEdital] = useState(false);
    const [tituloEdital, setTituloEdital] = useState("");
    const [descricaoEdital, setDescricaoEdital] = useState("");
    const [arquivoEdital, setArquivoEdital] = useState(null);
    const [enviandoEdital, setEnviandoEdital] = useState(false);
    const [termoBusca, setTermoBusca] = useState("");
    const navigate = useNavigate();

    useEffect(() => {
        fetchDados();
    }, []);

    const fetchDados = async () => {
        try {
            const token = localStorage.getItem("token");
            const headers = { Authorization: `Bearer ${token}` };

            const [noticiasRes, editaisRes, categoriasRes] = await Promise.all([
                axios.get("http://localhost:8081/api/artigos", { headers }),
                axios.get("http://localhost:8081/api/public/editais").catch(() => ({ data: { content: [] } })),
                axios.get("http://localhost:8081/api/categorias").catch(() => ({ data: [] })),
            ]);

            const noticiasData = noticiasRes.data.content || noticiasRes.data || [];
            const editaisData = editaisRes.data.content || editaisRes.data || [];
            const categoriasData = categoriasRes.data || [];

            setNoticias(noticiasData);
            setEditais(editaisData);
            setCategorias(categoriasData);
            setLoading(false);
        } catch (err) {
            console.error("Erro ao carregar dados:", err);
            setLoading(false);
        }
    };

    const calcularPorcentagensCategorias = () => {
        if (noticias.length === 0) return [];
        const contagem = {};
        noticias.forEach(noticia => {
            const categoriaNome = noticia.categoriaNome || "Sem Categoria";
            contagem[categoriaNome] = (contagem[categoriaNome] || 0) + 1;
        });
        return Object.entries(contagem).map(([nome, quantidade]) => ({
            nome,
            quantidade,
            porcentagem: ((quantidade / noticias.length) * 100).toFixed(1)
        }));
    };

    const calcularNoticiasPorMes = () => {
        const meses = {};
        noticias.forEach(noticia => {
            const data = new Date(noticia.dataPublicacao || noticia.dataCriacao);
            const mesAno = `${data.getMonth() + 1}/${data.getFullYear()}`;
            meses[mesAno] = (meses[mesAno] || 0) + 1;
        });
        return meses;
    };

    const totalGostei = noticias.reduce((acc, n) => acc + (n.gostei || 0), 0);
    const totalNeutro = noticias.reduce((acc, n) => acc + (n.neutro || 0), 0);
    const totalNaoGostei = noticias.reduce((acc, n) => acc + (n.naoGostei || 0), 0);

    const cores = ['#007bff', '#28a745', '#ffc107', '#dc3545', '#6f42c1', '#fd7e14'];

    const GraficoPizza = ({ dados, titulo }) => {
        let anguloInicial = 0;
        const total = dados.reduce((acc, item) => acc + item.quantidade, 0);
        return (
            <div style={{
                backgroundColor: 'white',
                padding: '24px',
                borderRadius: '12px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
            }}>
                <h3 style={{ marginTop: 0, marginBottom: '20px', textAlign: 'center' }}>{titulo}</h3>
                <div style={{ display: 'flex', alignItems: 'center', gap: '32px', flexWrap: 'wrap' }}>
                    <svg width="200" height="200" viewBox="0 0 200 200">
                        {dados.map((item, index) => {
                            const angulo = (item.quantidade / total) * 360;
                            const x1 = 100 + 90 * Math.cos((anguloInicial - 90) * Math.PI / 180);
                            const y1 = 100 + 90 * Math.sin((anguloInicial - 90) * Math.PI / 180);
                            anguloInicial += angulo;
                            const x2 = 100 + 90 * Math.cos((anguloInicial - 90) * Math.PI / 180);
                            const y2 = 100 + 90 * Math.sin((anguloInicial - 90) * Math.PI / 180);
                            const largeArc = angulo > 180 ? 1 : 0;
                            return (
                                <path
                                    key={index}
                                    d={`M 100 100 L ${x1} ${y1} A 90 90 0 ${largeArc} 1 ${x2} ${y2} Z`}
                                    fill={cores[index % cores.length]}
                                />
                            );
                        })}
                    </svg>
                    <div style={{ flex: 1, minWidth: '200px' }}>
                        {dados.map((item, index) => (
                            <div key={index} style={{
                                display: 'flex',
                                alignItems: 'center',
                                marginBottom: '12px',
                                fontSize: '14px'
                            }}>
                                <div style={{
                                    width: '16px',
                                    height: '16px',
                                    backgroundColor: cores[index % cores.length],
                                    marginRight: '8px',
                                    borderRadius: '3px'
                                }}></div>
                                <span style={{ flex: 1 }}>{item.nome}</span>
                                <span style={{ fontWeight: '600' }}>
                                    {item.quantidade} ({item.porcentagem}%)
                                </span>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        );
    };

    GraficoPizza.propTypes = {
        dados: PropTypes.arrayOf(PropTypes.shape({
            nome: PropTypes.string.isRequired,
            quantidade: PropTypes.number.isRequired,
            porcentagem: PropTypes.string.isRequired
        })).isRequired,
        titulo: PropTypes.string.isRequired
    };

    // Pesquisa
    const noticiasFiltradas = noticias.filter(noticia => {
        const t = termoBusca.trim().toLowerCase();
        if (!t) return true;
        return (
            (noticia.titulo && noticia.titulo.toLowerCase().includes(t)) ||
            (noticia.categoriaNome && noticia.categoriaNome.toLowerCase().includes(t))
        );
    });

    const handleCriarEdital = async (e) => {
        e.preventDefault();
        if (!arquivoEdital || !tituloEdital) {
            alert("Preencha título e arquivo!");
            return;
        }

        const token = localStorage.getItem("token");
        setEnviandoEdital(true);

        try {
            const formData = new FormData();
            formData.append("imagem", arquivoEdital);

            const uploadResponse = await axios.post(
                "http://localhost:8081/api/upload",
                formData,
                { headers: { Authorization: `Bearer ${token}` } }
            );

            await axios.post(
                "http://localhost:8081/api/editais",
                {
                    titulo: tituloEdital.trim(),
                    descricao: descricaoEdital.trim() || null,
                    arquivoUrl: uploadResponse.data.url,
                    arquivoNome: arquivoEdital.name
                },
                { headers: { Authorization: `Bearer ${token}` } }
            );

            alert("✅ Edital publicado com sucesso!");
            setTituloEdital("");
            setDescricaoEdital("");
            setArquivoEdital(null);
            setMostrarFormEdital(false);
            fetchDados();
        } catch (err) {
            alert("❌ Erro: " + (err.response?.data?.message || err.message));
        } finally {
            setEnviandoEdital(false);
        }
    };

    const handleExcluir = async (id) => {
        if (!window.confirm("Deseja excluir esta notícia?")) return;
        try {
            const token = localStorage.getItem("token");
            await axios.delete(`http://localhost:8081/api/artigos/${id}`, {
                headers: { Authorization: `Bearer ${token}` }
            });
            alert("Notícia excluída!");
            fetchDados();
        } catch (err) {
            alert("Erro ao excluir: " + err.message);
        }
    };

    if (loading) {
        return (
            <div style={{
                minHeight: '100vh',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
            }}>
                <div style={{ textAlign: 'center' }}>
                    <div style={{
                        width: '50px',
                        height: '50px',
                        border: '4px solid #f3f3f3',
                        borderTop: '4px solid #007bff',
                        borderRadius: '50%',
                        animation: 'spin 1s linear infinite',
                        margin: '0 auto 16px'
                    }}></div>
                    <p>Carregando dashboard...</p>
                    <style>{`@keyframes spin { 0% { transform: rotate(0deg); } 100% { transform: rotate(360deg); }}`}</style>
                </div>
            </div>
        );
    }

    const dadosCategorias = calcularPorcentagensCategorias();
    const noticiasPorMes = calcularNoticiasPorMes();

    return (
        <div style={{ minHeight: "100vh", backgroundColor: "#f5f5f5" }}>
            {/* Header */}
            <header style={{
                backgroundColor: "#1a1a1a",
                color: "white",
                padding: "20px 40px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                flexWrap: "wrap",
                gap: "12px"
            }}>
                <h1 style={{ margin: 0 }}>📊 Dashboard - The Club</h1>
                <div style={{ display: "flex", gap: "12px", flexWrap: "wrap" }}>
                    <button
                        onClick={() => navigate("/")}
                        style={{
                            padding: "10px 20px",
                            backgroundColor: "#6c757d",
                            color: "white",
                            border: "none",
                            borderRadius: "6px",
                            cursor: "pointer",
                            fontSize: "14px"
                        }}
                    >
                        Home
                    </button>
                    <button
                        onClick={() => {
                            localStorage.removeItem("token");
                            navigate("/admin/login");
                        }}
                        style={{
                            padding: "10px 20px",
                            backgroundColor: "#dc3545",
                            color: "white",
                            border: "none",
                            borderRadius: "6px",
                            cursor: "pointer",
                            fontSize: "14px"
                        }}
                    >
                        Sair
                    </button>
                </div>
            </header>

            <main style={{ maxWidth: "1800px", margin: "40px auto", padding: "0 20px" }}>
                {/* Cards de Estatísticas */}
                <div style={{
                    display: "grid",
                    gridTemplateColumns: "repeat(auto-fit, minmax(220px, 1fr))",
                    gap: "20px",
                    marginBottom: "40px"
                }}>
                    <div style={{
                        backgroundColor: "white",
                        padding: "24px",
                        borderRadius: "12px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                        borderLeft: "4px solid #007bff"
                    }}>
                        <h3 style={{ margin: "0 0 8px 0", color: "#666", fontSize: "14px", fontWeight: "500" }}>
                            Total de Notícias
                        </h3>
                        <p style={{ margin: 0, fontSize: "36px", fontWeight: "700", color: "#007bff" }}>
                            {noticias.length}
                        </p>
                    </div>
                    <div style={{
                        backgroundColor: "white",
                        padding: "24px",
                        borderRadius: "12px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                        borderLeft: "4px solid #28a745"
                    }}>
                        <h3 style={{ margin: "0 0 8px 0", color: "#666", fontSize: "14px", fontWeight: "500" }}>
                            Total de Visualizações
                        </h3>
                        <p style={{ margin: 0, fontSize: "36px", fontWeight: "700", color: "#28a745" }}>
                            {noticias.reduce((acc, n) => acc + (n.visualizacoes || 0), 0)}
                        </p>
                    </div>
                    <div style={{
                        backgroundColor: "white",
                        padding: "24px",
                        borderRadius: "12px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                        borderLeft: "4px solid #ffc107"
                    }}>
                        <h3 style={{ margin: "0 0 8px 0", color: "#666", fontSize: "14px", fontWeight: "500" }}>
                            Total de Editais
                        </h3>
                        <p style={{ margin: 0, fontSize: "36px", fontWeight: "700", color: "#ffc107" }}>
                            {editais.length}
                        </p>
                    </div>
                    <div style={{
                        background: "#e0f7fa",
                        borderRadius: "12px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                        padding: "24px",
                        minWidth: "110px",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        borderLeft: "4px solid #26c6da"
                    }}>
                        <h4 style={{ margin: "0 0 4px 0", color: "#158087", fontWeight: "600", fontSize: "14px" }}>GOSTEI</h4>
                        <span style={{ fontSize: "32px", fontWeight: "700", color: "#039be5" }}>
                            {totalGostei || 0}
                        </span>
                    </div>
                    <div style={{
                        background: "#fff3e0",
                        borderRadius: "12px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                        padding: "24px",
                        minWidth: "110px",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        borderLeft: "4px solid #ffb300"
                    }}>
                        <h4 style={{ margin: "0 0 4px 0", color: "#bb8600", fontWeight: "600", fontSize: "14px" }}>NEUTRO</h4>
                        <span style={{ fontSize: "32px", fontWeight: "700", color: "#ff9800" }}>
                            {totalNeutro || 0}
                        </span>
                    </div>
                    <div style={{
                        background: "#ffebee",
                        borderRadius: "12px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                        padding: "24px",
                        minWidth: "110px",
                        display: "flex",
                        flexDirection: "column",
                        alignItems: "center",
                        borderLeft: "4px solid #e57373"
                    }}>
                        <h4 style={{ margin: "0 0 4px 0", color: "#c62828", fontWeight: "600", fontSize: "14px" }}>NÃO GOSTEI</h4>
                        <span style={{ fontSize: "32px", fontWeight: "700", color: "#d32f2f" }}>
                            {totalNaoGostei || 0}
                        </span>
                    </div>
                </div>

                {/* Gráficos */}
                <div style={{
                    display: "grid",
                    gridTemplateColumns: "repeat(auto-fit, minmax(400px, 1fr))",
                    gap: "24px",
                    marginBottom: "40px"
                }}>
                    {dadosCategorias.length > 0 && (
                        <GraficoPizza
                            dados={dadosCategorias}
                            titulo="Notícias por Categoria"
                        />
                    )}

                    <div style={{
                        backgroundColor: "white",
                        padding: "24px",
                        borderRadius: "12px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
                    }}>
                        <h3 style={{ marginTop: 0, marginBottom: '20px' }}>Publicações por Mês</h3>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                            {Object.entries(noticiasPorMes).slice(0, 6).map(([mes, quantidade], index) => (
                                <div key={index} style={{
                                    display: 'flex',
                                    alignItems: 'center',
                                    gap: '12px'
                                }}>
                                    <span style={{
                                        minWidth: '60px',
                                        fontSize: '14px',
                                        fontWeight: '500'
                                    }}>
                                        {mes}
                                    </span>
                                    <div style={{
                                        flex: 1,
                                        height: '24px',
                                        backgroundColor: '#e9ecef',
                                        borderRadius: '12px',
                                        overflow: 'hidden',
                                        position: 'relative'
                                    }}>
                                        <div style={{
                                            width: `${(quantidade / Math.max(...Object.values(noticiasPorMes))) * 100}%`,
                                            height: '100%',
                                            backgroundColor: cores[index % cores.length],
                                            transition: 'width 0.3s ease'
                                        }}></div>
                                    </div>
                                    <span style={{
                                        minWidth: '40px',
                                        textAlign: 'right',
                                        fontWeight: '600',
                                        fontSize: '14px'
                                    }}>
                                        {quantidade}
                                    </span>
                                </div>
                            ))}
                        </div>
                    </div>
                </div>

                {/* Botões de Ação + Pesquisa */}
                <div style={{
                    display: "flex",
                    gap: "12px",
                    marginBottom: "32px",
                    flexWrap: "wrap",
                    alignItems: "center"
                }}>
                    <button
                        onClick={() => navigate("/admin/artigos/novo")}
                        style={{
                            padding: "12px 24px",
                            backgroundColor: "#007bff",
                            color: "white",
                            border: "none",
                            borderRadius: "8px",
                            cursor: "pointer",
                            fontSize: "16px",
                            fontWeight: "600"
                        }}
                    >
                        Nova Notícia
                    </button>
                    <button
                        onClick={() => setMostrarFormEdital(!mostrarFormEdital)}
                        style={{
                            padding: "12px 24px",
                            backgroundColor: mostrarFormEdital ? "#dc3545" : "#28a745",
                            color: "white",
                            border: "none",
                            borderRadius: "8px",
                            cursor: "pointer",
                            fontSize: "16px",
                            fontWeight: "600"
                        }}
                    >
                        {mostrarFormEdital ? "Cancelar" : "Novo Edital"}
                    </button>
                    <input
                        type="text"
                        value={termoBusca}
                        onChange={e => setTermoBusca(e.target.value)}
                        placeholder="Pesquisar por título ou categoria..."
                        style={{
                            padding: "12px",
                            border: "2px solid #e0e0e0",
                            borderRadius: "8px",
                            fontSize: "15px",
                            minWidth: "320px",
                            flex: "1 1 320px"
                        }}
                    />
                </div>
                {/* Formulário de Edital */}
                {mostrarFormEdital && (
                    <div style={{
                        backgroundColor: "white",
                        padding: "32px",
                        borderRadius: "12px",
                        marginBottom: "32px",
                        boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
                    }}>
                        <h2 style={{ marginTop: 0 }}>Novo Edital</h2>
                        <form onSubmit={handleCriarEdital}>
                            <div style={{ marginBottom: "20px" }}>
                                <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                                    Título *
                                </label>
                                <input
                                    type="text"
                                    value={tituloEdital}
                                    onChange={(e) => setTituloEdital(e.target.value)}
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
                            <div style={{ marginBottom: "20px" }}>
                                <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                                    Descrição
                                </label>
                                <textarea
                                    value={descricaoEdital}
                                    onChange={(e) => setDescricaoEdital(e.target.value)}
                                    placeholder="Descreva o edital..."
                                    rows="4"
                                    style={{
                                        width: "100%",
                                        padding: "12px",
                                        border: "2px solid #e0e0e0",
                                        borderRadius: "8px",
                                        fontSize: "14px",
                                        fontFamily: "inherit"
                                    }}
                                />
                            </div>
                            <div style={{ marginBottom: "30px" }}>
                                <label style={{ display: "block", marginBottom: "8px", fontWeight: "500" }}>
                                    Arquivo PDF *
                                </label>
                                <input
                                    type="file"
                                    accept=".pdf"
                                    onChange={(e) => setArquivoEdital(e.target.files[0])}
                                    required
                                    style={{
                                        width: "100%",
                                        padding: "12px",
                                        border: "2px solid #e0e0e0",
                                        borderRadius: "8px"
                                    }}
                                />
                                {arquivoEdital && (
                                    <p style={{ marginTop: "8px", color: "#28a745" }}>
                                        {arquivoEdital.name}
                                    </p>
                                )}
                            </div>
                            <button
                                type="submit"
                                disabled={enviandoEdital}
                                style={{
                                    width: "100%",
                                    padding: "14px",
                                    backgroundColor: enviandoEdital ? "#ccc" : "#28a745",
                                    color: "white",
                                    border: "none",
                                    borderRadius: "8px",
                                    fontSize: "16px",
                                    fontWeight: "600",
                                    cursor: enviandoEdital ? "not-allowed" : "pointer"
                                }}
                            >
                                {enviandoEdital ? "Publicando..." : "Publicar Edital"}
                            </button>
                        </form>
                    </div>
                )}
                {/* Lista de Notícias */}
                <div style={{
                    backgroundColor: "white",
                    padding: "24px",
                    borderRadius: "12px",
                    boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
                }}>
                    <h2 style={{ marginTop: 0 }}>Notícias Publicadas</h2>
                    {noticiasFiltradas.length > 0 ? (
                        <div style={{ overflowX: 'auto' }}>
                            <table style={{ width: "100%", borderCollapse: "collapse", minWidth: '600px' }}>
                                <thead>
                                <tr style={{ borderBottom: "2px solid #e0e0e0", backgroundColor: '#f8f9fa' }}>
                                    <th style={{ padding: "12px", textAlign: "left" }}>Título</th>
                                    <th style={{ padding: "12px", textAlign: "left" }}>Categoria</th>
                                    <th style={{ padding: "12px", textAlign: "center" }}>Visualizações / Avaliações</th>
                                    <th style={{ padding: "12px", textAlign: "center" }}>Ações</th>
                                </tr>
                                </thead>
                                <tbody>
                                {noticiasFiltradas.map(noticia => (
                                    <tr key={noticia.id} style={{ borderBottom: "1px solid #f0f0f0" }}>
                                        <td style={{ padding: "12px" }}>{noticia.titulo}</td>
                                        <td style={{ padding: "12px" }}>
                                                <span style={{
                                                    padding: "4px 12px",
                                                    backgroundColor: "#e9ecef",
                                                    borderRadius: "12px",
                                                    fontSize: "12px"
                                                }}>
                                                    {noticia.categoriaNome || "Sem categoria"}
                                                </span>
                                        </td>
                                        <td style={{ padding: "12px", textAlign: "center", fontWeight: '600' }}>
                                            <div style={{ marginBottom: "8px" }}>
                                                Visualizações: {noticia.visualizacoes || 0}
                                            </div>
                                            <div style={{ display: "flex", justifyContent: "center", gap: "8px" }}>
                                                <div style={{
                                                    background: "#e0f7fa",
                                                    borderRadius: "8px",
                                                    padding: "6px 16px",
                                                    fontSize: "13px",
                                                    fontWeight: 600,
                                                    minWidth: "70px"
                                                }}>
                                                    GOSTEI<br />{noticia.gostei || 0}
                                                </div>
                                                <div style={{
                                                    background: "#fff3e0",
                                                    borderRadius: "8px",
                                                    padding: "6px 16px",
                                                    fontSize: "13px",
                                                    fontWeight: 600,
                                                    minWidth: "70px"
                                                }}>
                                                    NEUTRO<br />{noticia.neutro || 0}
                                                </div>
                                                <div style={{
                                                    background: "#ffebee",
                                                    borderRadius: "8px",
                                                    padding: "6px 12px",
                                                    fontSize: "13px",
                                                    fontWeight: 600,
                                                    minWidth: "90px"
                                                }}>
                                                    NÃO GOSTEI<br />{noticia.naoGostei || 0}
                                                </div>
                                            </div>
                                        </td>
                                        <td style={{ padding: "12px", textAlign: "center" }}>
                                            <button
                                                onClick={() => navigate(`/admin/artigos/editar/${noticia.id}`)}
                                                style={{
                                                    padding: "6px 12px",
                                                    backgroundColor: "#007bff",
                                                    color: "white",
                                                    border: "none",
                                                    borderRadius: "4px",
                                                    cursor: "pointer",
                                                    marginRight: "8px",
                                                    fontSize: "13px"
                                                }}
                                            >
                                                Editar
                                            </button>
                                            <button
                                                onClick={() => handleExcluir(noticia.id)}
                                                style={{
                                                    padding: "6px 12px",
                                                    backgroundColor: "#dc3545",
                                                    color: "white",
                                                    border: "none",
                                                    borderRadius: "4px",
                                                    cursor: "pointer",
                                                    fontSize: "13px"
                                                }}
                                            >
                                                Excluir
                                            </button>
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    ) : (
                        <p style={{ textAlign: "center", color: "#666", padding: "40px" }}>
                            Nenhuma notícia publicada ainda.
                        </p>
                    )}
                </div>
            </main>
        </div>
    );
};

export default Dashboard;
