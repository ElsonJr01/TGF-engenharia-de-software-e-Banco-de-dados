import React, { useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const Cadastro = () => {
    const [nome, setNome] = useState("");
    const [email, setEmail] = useState("");
    const [senha, setSenha] = useState("");
    const [confirmarSenha, setConfirmarSenha] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleCadastro = async (e) => {
        e.preventDefault();
        setError("");
        if (senha !== confirmarSenha) {
            setError("As senhas não correspondem.");
            return;
        }
        setLoading(true);

        try {
            await axios.post("http://localhost:8081/api/auth/register", {
                nome,
                email,
                senha,
            });
            setLoading(false);
            alert("Cadastro realizado com sucesso! Agora faça o login.");
            // Leva para a tela de login. Não autentica e não leva ao dashboard/admin.
            navigate("/admin/login");
        } catch (err) {
            console.error("Erro no cadastro:", err);
            setError(
                err.response?.data?.erro ||
                "Falha no cadastro, tente novamente."
            );
            setLoading(false);
        }
    };

    const handleLogin = () => {
        navigate("/admin/login");
    };

    return (
        <div style={{
            minHeight: "100vh",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            backgroundColor: "#f5f5f5"
        }}>
            <div style={{
                width: "100%",
                maxWidth: "400px",
                padding: "40px",
                backgroundColor: "white",
                borderRadius: "12px",
                boxShadow: "0 4px 16px rgba(0,0,0,0.1)"
            }}>
                <h1 style={{
                    fontSize: "28px",
                    fontWeight: "700",
                    marginBottom: "8px",
                    textAlign: "center",
                    color: "#1a1a1a"
                }}>
                    Cadastro
                </h1>
                <p style={{
                    fontSize: "14px",
                    color: "#666",
                    textAlign: "center",
                    marginBottom: "32px"
                }}>
                    Inscreva-se para acessar o sistema
                </p>

                {error && (
                    <div style={{
                        padding: "12px",
                        marginBottom: "20px",
                        backgroundColor: "#fee",
                        border: "1px solid #fcc",
                        borderRadius: "6px",
                        color: "#c33",
                        fontSize: "14px"
                    }}>
                        {error}
                    </div>
                )}

                <form onSubmit={handleCadastro}>
                    <div style={{ marginBottom: "20px" }}>
                        <label style={{
                            display: "block",
                            marginBottom: "8px",
                            fontSize: "14px",
                            fontWeight: "500",
                            color: "#333"
                        }}>
                            Nome
                        </label>
                        <input
                            type="text"
                            value={nome}
                            onChange={(e) => setNome(e.target.value)}
                            required
                            placeholder="Seu nome completo"
                            style={{
                                width: "100%",
                                padding: "12px",
                                border: "2px solid #e0e0e0",
                                borderRadius: "6px",
                                fontSize: "14px",
                                outline: "none"
                            }}
                            onFocus={e => e.target.style.borderColor = "#007bff"}
                            onBlur={e => e.target.style.borderColor = "#e0e0e0"}
                        />
                    </div>
                    <div style={{ marginBottom: "20px" }}>
                        <label style={{
                            display: "block",
                            marginBottom: "8px",
                            fontSize: "14px",
                            fontWeight: "500",
                            color: "#333"
                        }}>
                            Email
                        </label>
                        <input
                            type="email"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                            required
                            placeholder="seu@email.com"
                            style={{
                                width: "100%",
                                padding: "12px",
                                border: "2px solid #e0e0e0",
                                borderRadius: "6px",
                                fontSize: "14px",
                                outline: "none"
                            }}
                            onFocus={e => e.target.style.borderColor = "#007bff"}
                            onBlur={e => e.target.style.borderColor = "#e0e0e0"}
                        />
                    </div>
                    <div style={{ marginBottom: "16px" }}>
                        <label style={{
                            display: "block",
                            marginBottom: "8px",
                            fontSize: "14px",
                            fontWeight: "500",
                            color: "#333"
                        }}>
                            Senha
                        </label>
                        <input
                            type="password"
                            value={senha}
                            onChange={(e) => setSenha(e.target.value)}
                            required
                            placeholder="••••••••"
                            style={{
                                width: "100%",
                                padding: "12px",
                                border: "2px solid #e0e0e0",
                                borderRadius: "6px",
                                fontSize: "14px",
                                outline: "none"
                            }}
                            onFocus={e => e.target.style.borderColor = "#007bff"}
                            onBlur={e => e.target.style.borderColor = "#e0e0e0"}
                        />
                    </div>
                    <div style={{ marginBottom: "24px" }}>
                        <label style={{
                            display: "block",
                            marginBottom: "8px",
                            fontSize: "14px",
                            fontWeight: "500",
                            color: "#333"
                        }}>
                            Confirmar Senha
                        </label>
                        <input
                            type="password"
                            value={confirmarSenha}
                            onChange={(e) => setConfirmarSenha(e.target.value)}
                            required
                            placeholder="Repita sua senha"
                            style={{
                                width: "100%",
                                padding: "12px",
                                border: "2px solid #e0e0e0",
                                borderRadius: "6px",
                                fontSize: "14px",
                                outline: "none"
                            }}
                            onFocus={e => e.target.style.borderColor = "#007bff"}
                            onBlur={e => e.target.style.borderColor = "#e0e0e0"}
                        />
                    </div>
                    <button
                        type="submit"
                        disabled={loading}
                        style={{
                            width: "100%",
                            padding: "14px",
                            backgroundColor: loading ? "#ccc" : "#007bff",
                            color: "white",
                            border: "none",
                            borderRadius: "6px",
                            fontSize: "16px",
                            fontWeight: "600",
                            cursor: loading ? "not-allowed" : "pointer",
                            transition: "background-color 0.2s"
                        }}
                        onMouseEnter={e => {
                            if (!loading) e.target.style.backgroundColor = "#0056b3";
                        }}
                        onMouseLeave={e => {
                            if (!loading) e.target.style.backgroundColor = "#007bff";
                        }}
                    >
                        {loading ? "Cadastrando..." : "Criar conta"}
                    </button>
                </form>

                <div style={{ display: "flex", justifyContent: "center", gap: 8, marginTop: 24 }}>
                    <button
                        onClick={handleLogin}
                        style={{
                            background: "none",
                            color: "#007bff",
                            border: "none",
                            cursor: "pointer",
                            textDecoration: "underline",
                            fontSize: "14px"
                        }}
                    >
                        Já tem conta? Fazer Login
                    </button>
                </div>
                <p style={{
                    marginTop: "20px",
                    textAlign: "center",
                    fontSize: "14px",
                    color: "#666"
                }}>
                    <a href="/" style={{ color: "#007bff", textDecoration: "none" }}>
                        ← Voltar para Home
                    </a>
                </p>
            </div>
        </div>
    );
};

export default Cadastro;
