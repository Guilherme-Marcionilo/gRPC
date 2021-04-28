package br.com.zup

import io.grpc.Channel
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import javax.inject.Singleton

@MicronautTest(transactional = false)
internal class CarrosControllerTest(
    val repository: CarroRepository,
    val grpcClient: CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub
) {

    @BeforeEach
    fun setup() {
        repository.deleteAll()
    }

    @Test
    fun `deve criar um novo carro`() {
        //cenário

        //ação
        val response = grpcClient.adicionar(CarroRequest.newBuilder().setModelo("Gol").setPlaca("A21312").build())
        //validação
        assertNotNull(response.id)
        assertTrue(repository.existsById(response.id))
    }

    @Test
    fun `NÃO deve adicionar novo carro quando já existir um carro com a mesma placa!`() {
        val existente = repository.save(Carro(modelo = "Fiat", placa = "12312"))


        val erro = assertThrows<StatusRuntimeException> {
            grpcClient.adicionar(
                CarroRequest.newBuilder()
                    .setModelo("Ferrari")
                    .setPlaca(existente.placa)
                    .build()
            )
        }

        assertEquals(Status.ALREADY_EXISTS.code, erro.status.code)
        assertEquals("OPS! Carro com placa existente!", erro.status.description)


    }


    @Factory
    class Clients {
        @Singleton
        fun blockingStub(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): CarrosGrpcServiceGrpc.CarrosGrpcServiceBlockingStub? {
            return CarrosGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

}


