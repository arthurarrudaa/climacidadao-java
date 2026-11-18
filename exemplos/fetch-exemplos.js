// Exemplos para o React (Vite). Nao e um front-end completo.
const API_URL = "http://localhost:8081";

// GET: listar usuarios
export async function listarUsuarios() {
    const resposta = await fetch(`${API_URL}/usuarios`);
    if (!resposta.ok) throw new Error("Nao foi possivel listar usuarios.");
    return await resposta.json();
}

// POST: cadastrar usuario
export async function cadastrarUsuario(nome, email) {
    const resposta = await fetch(`${API_URL}/usuarios`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ nome, email })
    });
    if (!resposta.ok) throw new Error("Nao foi possivel cadastrar usuario.");
    return await resposta.json();
}

// GET: listar favoritos de um usuario
export async function listarFavoritos(usuarioId) {
    const resposta = await fetch(`${API_URL}/usuarios/${usuarioId}/favoritos`);
    if (!resposta.ok) throw new Error("Nao foi possivel listar favoritos.");
    return await resposta.json();
}

// POST: vincular cidade favorita ao usuario
export async function cadastrarFavorito(usuarioId, cidade) {
    const resposta = await fetch(`${API_URL}/usuarios/${usuarioId}/favoritos`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ cidade })
    });
    if (!resposta.ok) {
        const erro = await resposta.json();
        throw new Error(erro.mensagem); // Ex.: favorito repetido, status 409
    }
    return await resposta.json();
}
