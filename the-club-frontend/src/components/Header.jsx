import React, { useState } from "react";
import { useNavigate, useLocation } from "react-router-dom";

const Header = () => {
    const [menuOpen, setMenuOpen] = useState(false);
    const navigate = useNavigate();
    const location = useLocation();

    const toggleMenu = () => {
        setMenuOpen(!menuOpen);
    };

    const handleNavigation = (path) => {
        navigate(path);
        setMenuOpen(false);
    };

    return (
        <header style={{
            backgroundColor: "#1a1a1a",
            color: "white",
            boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
            position: "sticky",
            top: 0,
            zIndex: 1000
        }}>
            {/* Container principal */}
            <div style={{
                maxWidth: "1200px",
                margin: "0 auto",
                padding: "0 20px"
            }}>
                {/* Barra superior */}
                <div style={{
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "space-between",
                    padding: "16px 0"
                }}>
                    {/* Logo e Nome do Jornal */}
                    <div
                        style={{
                            display: "flex",
                            alignItems: "center",
                            gap: "16px",
                            cursor: "pointer"
                        }}
                        onClick={() => handleNavigation("/")}
                    >
                        {/* Espaço para Logo (círculo placeholder) */}
                        <div style={{
                            width: "50px",
                            height: "50px",
                            borderRadius: "50%",
                            backgroundColor: "#007bff",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            fontSize: "24px",
                            fontWeight: "700",
                            color: "white"
                        }}>
                            TC
                        </div>

                        {/* Nome e Slogan */}
                        <div>
                            <h1 style={{
                                fontSize: "24px",
                                fontWeight: "700",
                                margin: 0,
                                color: "white"
                            }}>
                                The Club
                            </h1>
                            <p style={{
                                fontSize: "12px",
                                color: "#ccc",
                                margin: 0
                            }}>
                                O seu jornal universitário
                            </p>
                        </div>
                    </div>

                    {/* Navegação Desktop */}
                    <nav style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "24px"
                    }}>
                        {/* Links de navegação - esconder no mobile */}
                        <div style={{
                            display: window.innerWidth > 768 ? "flex" : "none",
                            gap: "24px",
                            alignItems: "center"
                        }}>
                            <a
                                href="/"
                                style={{
                                    color: location.pathname === "/" ? "#007bff" : "white",
                                    textDecoration: "none",
                                    fontSize: "14px",
                                    fontWeight: "500",
                                    transition: "color 0.2s"
                                }}
                                onMouseEnter={(e) => e.target.style.color = "#007bff"}
                                onMouseLeave={(e) => e.target.style.color = location.pathname === "/" ? "#007bff" : "white"}
                            >
                                Início
                            </a>
                            <a
                                href="/categorias"
                                style={{
                                    color: "white",
                                    textDecoration: "none",
                                    fontSize: "14px",
                                    fontWeight: "500",
                                    transition: "color 0.2s"
                                }}
                                onMouseEnter={(e) => e.target.style.color = "#007bff"}
                                onMouseLeave={(e) => e.target.style.color = "white"}
                            >
                                Categorias
                            </a>
                            <a
                                href="/sobre"
                                style={{
                                    color: "white",
                                    textDecoration: "none",
                                    fontSize: "14px",
                                    fontWeight: "500",
                                    transition: "color 0.2s"
                                }}
                                onMouseEnter={(e) => e.target.style.color = "#007bff"}
                                onMouseLeave={(e) => e.target.style.color = "white"}
                            >
                                Sobre
                            </a>
                        </div>

                        {/* Botão de Login */}
                        <button
                            onClick={() => handleNavigation("/admin/login")}
                            style={{
                                padding: "8px 20px",
                                backgroundColor: "#007bff",
                                color: "white",
                                border: "none",
                                borderRadius: "6px",
                                fontSize: "14px",
                                fontWeight: "600",
                                cursor: "pointer",
                                transition: "background-color 0.2s",
                                display: window.innerWidth > 768 ? "block" : "none"
                            }}
                            onMouseEnter={(e) => e.target.style.backgroundColor = "#0056b3"}
                            onMouseLeave={(e) => e.target.style.backgroundColor = "#007bff"}
                        >
                            Login
                        </button>

                        {/* Menu Hamburger (Mobile) */}
                        <button
                            onClick={toggleMenu}
                            style={{
                                display: window.innerWidth <= 768 ? "block" : "none",
                                background: "none",
                                border: "none",
                                color: "white",
                                fontSize: "24px",
                                cursor: "pointer",
                                padding: "8px"
                            }}
                        >
                            ☰
                        </button>
                    </nav>
                </div>

                {/* Menu Mobile (dropdown) */}
                {menuOpen && (
                    <div style={{
                        padding: "16px 0",
                        borderTop: "1px solid #333"
                    }}>
                        <a
                            href="/"
                            style={{
                                display: "block",
                                color: "white",
                                textDecoration: "none",
                                padding: "12px 0",
                                fontSize: "14px",
                                fontWeight: "500"
                            }}
                            onClick={() => setMenuOpen(false)}
                        >
                            Início
                        </a>
                        <a
                            href="/categorias"
                            style={{
                                display: "block",
                                color: "white",
                                textDecoration: "none",
                                padding: "12px 0",
                                fontSize: "14px",
                                fontWeight: "500"
                            }}
                            onClick={() => setMenuOpen(false)}
                        >
                            Categorias
                        </a>
                        <a
                            href="/sobre"
                            style={{
                                display: "block",
                                color: "white",
                                textDecoration: "none",
                                padding: "12px 0",
                                fontSize: "14px",
                                fontWeight: "500"
                            }}
                            onClick={() => setMenuOpen(false)}
                        >
                            Sobre
                        </a>
                        <button
                            onClick={() => handleNavigation("/admin/login")}
                            style={{
                                marginTop: "12px",
                                width: "100%",
                                padding: "10px",
                                backgroundColor: "#007bff",
                                color: "white",
                                border: "none",
                                borderRadius: "6px",
                                fontSize: "14px",
                                fontWeight: "600",
                                cursor: "pointer"
                            }}
                        >
                            Login
                        </button>
                    </div>
                )}
            </div>
        </header>
    );
};

export default Header;
