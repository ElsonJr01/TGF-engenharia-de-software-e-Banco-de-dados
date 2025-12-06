//package dominio.dto.request;
//
//import jakarta.validation.constraints.*;
//import lombok.*;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class ComentarioRequestDTO {
//
//    @NotBlank(message = "Comentário não pode estar vazio")
//    @Size(min = 3, max = 1000, message = "Comentário deve ter entre 3 e 1000 caracteres")
//    private String comentario;
//
//    @NotNull(message = "Artigo é obrigatório")
//    private Long artigoId;
//
//    @NotNull(message = "Usuário é obrigatório")
//    private Long usuarioId;
//}
