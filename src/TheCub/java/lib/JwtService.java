package lib; // Pacote de infraestrutura/segurança (JWT)

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

/**
 * Serviço responsável por:
 * - Gerar tokens JWT
 * - Validar tokens JWT
 * - Extrair informações (claims) de dentro do token
 *
 * Usa a biblioteca jjwt para lidar com assinatura e parsing.
 */
@Service // Registrado como bean de serviço no Spring
@Slf4j   // Habilita logs com 'log.info', 'log.warn', etc.
public class JwtService {

    // Chave secreta usada para assinar e validar o token.
    // Vem do application.properties: jwt.secret=...
    @Value("${jwt.secret}")
    private String secretKey;

    // Tempo de expiração do token em milissegundos.
    // Ex.: jwt.expiration=3600000 (1 hora)
    @Value("${jwt.expiration}")
    private Long jwtExpirationMs;

    /**
     * Constrói a SecretKey usada na assinatura/validação do JWT.
     *
     * - Decodifica a string base64 em bytes
     * - Usa algoritmo HMAC com a chave resultante
     */
    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Gera um token JWT com:
     * - subject = username (email do usuário)
     * - claims extras (roles, id, etc. se você quiser)
     * - data de emissão
     * - data de expiração
     * - assinatura com a chave secreta
     */
    public String generateToken(String username, Map<String, Object> extraClaims) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .setClaims(extraClaims)   // payload customizado
                .setSubject(username)     // quem é o "dono" do token (email)
                .setIssuedAt(now)         // quando foi gerado
                .setExpiration(expiryDate)// quando expira
                .signWith(getSignKey())   // assina com HMAC e secretKey
                .compact();               // gera a string final do token
    }

    /**
     * Sobrecarga simples: gera token sem claims extras.
     */
    public String generateToken(String username) {
        return generateToken(username, Map.of());
    }

    /**
     * Extrai o "username" (subject) de dentro do token.
     * No seu caso, é o e-mail.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrai uma claim genérica do token, usando uma função que recebe Claims
     * e devolve algo (subject, expiration, etc.).
     *
     * Também trata:
     * - ExpiredJwtException -> lança JwtException com mensagem amigável
     * - JwtException genérica -> token inválido ou corrompido
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        try {
            JwtParser parser = Jwts.parserBuilder()
                    .setSigningKey(getSignKey()) // chave para validar a assinatura
                    .build();

            // Faz o parse do token assinando e verificando integridade
            Claims claims = parser.parseClaimsJws(token).getBody();

            // Aplica a função para extrair a claim desejada
            return claimsResolver.apply(claims);

        } catch (ExpiredJwtException e) {
            // Token com data de expiração ultrapassada
            log.warn("⚠️ Token expirado: {}", e.getMessage());
            throw new JwtException("Token expirado. Faça login novamente.");
        } catch (JwtException e) {
            // Qualquer outro problema com o token (assinatura, formato, etc.)
            log.error("❌ Token inválido: {}", e.getMessage());
            throw new JwtException("Token JWT inválido ou corrompido.");
        }
    }

    /**
     * Verifica se o token é válido para o username informado:
     * - subject do token precisa ser igual ao username
     * - token não pode estar expirado
     *
     * Se qualquer problema de JWT acontecer, retorna false.
     */
    public boolean isTokenValid(String token, String username) {
        try {
            String extractedUsername = extractUsername(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Checa se o token está expirado comparando a data de expiração
     * com o horário atual.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Extrai a data de expiração do token.
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
