package com.b2bmatch.catalogo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.b2bmatch.catalogo.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Baja y reactivacion de category y skill.
 *
 * Estos tests fijan el contrato de la fase 2 en catalogo, que era el punto mas
 * fragil de la transicion: el borrado dejo de escribir `status='DELETED'`, pero
 * los endpoints de restore seguian haciendo
 * `status = COALESCE(previous_status, 'ACTIVE')`. Como nadie escribia
 * `previous_status`, ese COALESCE caia siempre en 'ACTIVE', y restaurar una
 * categoria INACTIVE la promovia a ACTIVE.
 *
 * `status` es estado de negocio; la baja vive en `deleted_at`. Por eso `restore`
 * solo debe despejar `deleted_at` y no escribir `status`.
 */
@DisplayName("Baja y restauracion de category y skill")
class TaxonomiaBajaTest extends AbstractIntegrationTest {

    @Autowired
    private WebApplicationContext contexto;

    private MockMvc mvc;

    private final ObjectMapper json = new ObjectMapper();

    private MockMvc mvc() {
        if (mvc == null) {
            // `springSecurity()` es necesario: sin la cadena de filtros los
            // post-processors por request no aplican y cada prueba se ejecutaria
            // siempre como el usuario de @WithMockUser. Con ella se ejercita el
            // mismo camino que en produccion, @PreAuthorize incluido.
            mvc = MockMvcBuilders.webAppContextSetup(contexto)
                    .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers
                            .springSecurity())
                    .build();
        }
        return mvc;
    }

    private void bajaYReactivaCategoria(Long id) throws Exception {
        mvc().perform(delete("/api/catalogo/categories/{id}", id)).andExpect(status().isNoContent());
        mvc().perform(put("/api/catalogo/categories/{id}/restore", id)).andExpect(status().isOk());
    }

    private void bajaYReactivaSkill(Long id) throws Exception {
        mvc().perform(delete("/api/catalogo/skills/{id}", id)).andExpect(status().isNoContent());
        mvc().perform(put("/api/catalogo/skills/{id}/restore", id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("borrar una categoria no toca su estado de negocio")
    void borrarCategoriaNoTocaElEstado() throws Exception {
        Long id = crearCategoria("Software");

        mvc().perform(delete("/api/catalogo/categories/{id}", id)).andExpect(status().isNoContent());

        assertThat(estaBorradaCategoria(id)).isTrue();
        assertThat(estadoCategoria(id)).isEqualTo("ACTIVE");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("una categoria borrada desaparece del listado y del detalle")
    void laCategoriaBorradaDesaparece() throws Exception {
        Long id = crearCategoria("Software");

        mvc().perform(delete("/api/catalogo/categories/{id}", id)).andExpect(status().isNoContent());

        String listado = mvc().perform(get("/api/catalogo/categories"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(listado).doesNotContain("Software");
        mvc().perform(get("/api/catalogo/categories/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("RESTAURAR NO promueve a ACTIVE una categoria INACTIVE (regresion)")
    void restaurarNoPromueveCategoriaInactiva() throws Exception {
        Long id = crearCategoria("Software");
        // La API no tiene endpoint para desactivar una categoria, asi que el
        // estado se fuerza por SQL. El defecto es real igualmente: `restore`
        // escribia en `status` usando una columna que nadie llenaba.
        jdbc.update("UPDATE catalogo.category SET status = 'INACTIVE' WHERE id = ?", id);

        mvc().perform(delete("/api/catalogo/categories/{id}", id)).andExpect(status().isNoContent());
        assertThat(estadoCategoria(id)).isEqualTo("INACTIVE");

        mvc().perform(put("/api/catalogo/categories/{id}/restore", id)).andExpect(status().isOk());

        // El estado de negocio debe sobrevivir al viaje de ida y vuelta.
        assertThat(estadoCategoria(id)).isEqualTo("INACTIVE");
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("RESTAURAR NO promueve a ACTIVE una skill SUSPENDED (regresion)")
    void restaurarNoPromueveSkillSuspendida() throws Exception {
        Long id = crearSkill("Java");
        jdbc.update("UPDATE catalogo.skill SET status = 'SUSPENDED' WHERE id = ?", id);

        bajaYReactivaSkill(id);

        assertThat(estadoSkill(id)).isEqualTo("SUSPENDED");
        assertThat(estaBorradaSkill(id)).isFalse();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("round trip: borrar y restaurar deja la categoria como estaba")
    void roundTripDeCategoria() throws Exception {
        Long id = crearCategoria("Software");
        jdbc.update("UPDATE catalogo.category SET status = 'INACTIVE' WHERE id = ?", id);

        bajaYReactivaCategoria(id);

        assertThat(estaBorradaCategoria(id)).isFalse();
        assertThat(estadoCategoria(id)).isEqualTo("INACTIVE");
        // Y vuelve a estar disponible: el nombre sigue reservado por UNIQUE,
        // asi que el viaje de vuelta no duplica nada.
        mvc().perform(get("/api/catalogo/categories/{id}", id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("el nombre de una categoria borrada sigue reservado")
    void elNombreBorradoSigueReservado() throws Exception {
        Long id = crearCategoria("Software");
        mvc().perform(delete("/api/catalogo/categories/{id}", id)).andExpect(status().isNoContent());

        // UNIQUE no distingue borradas: sin esto, borrar y volver a crear
        // dejaria dos filas iguales y los listados ambiguan.
        mvc().perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .post("/api/catalogo/categories")
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .content(json.writeValueAsString(java.util.Map.of("name", "Software"))))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("un profesional no puede restaurar: restore es de ADMIN")
    void restaurarEsSoloDeAdmin() throws Exception {
        Long id = crearCategoria("Software");
        // El borrado tambien es de ADMIN, asi que se hace como ADMIN y solo la
        // reactivacion se intenta como profesional.
        mvc().perform(delete("/api/catalogo/categories/{id}", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("admin").roles("ADMIN")))
                .andExpect(status().isNoContent());

        mvc().perform(put("/api/catalogo/categories/{id}/restore", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("pro").roles("PROFESSIONAL")))
                .andExpect(status().isForbidden());
        assertThat(estaBorradaCategoria(id)).isTrue();
    }

    @Test
    @DisplayName("borrar tambien es de ADMIN, no basta con ser dueno")
    void borrarEsSoloDeAdmin() throws Exception {
        Long id = crearCategoria("Software");

        mvc().perform(delete("/api/catalogo/categories/{id}", id)
                .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors
                        .user("pro").roles("PROFESSIONAL")))
                .andExpect(status().isForbidden());
        assertThat(estaBorradaCategoria(id)).isFalse();
    }
}
