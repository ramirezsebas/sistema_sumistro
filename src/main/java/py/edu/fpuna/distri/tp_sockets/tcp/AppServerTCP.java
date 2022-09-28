package py.edu.fpuna.distri.tp_sockets.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import py.edu.fpuna.distri.tp_sockets.data.mappers.EnviarOrdenDataResponse;
import py.edu.fpuna.distri.tp_sockets.data.mappers.EnviarOrdenResponse;
import py.edu.fpuna.distri.tp_sockets.data.mappers.ListarSuministroResponse;
import py.edu.fpuna.distri.tp_sockets.data.mappers.RegistrarConsumoDataResponse;
import py.edu.fpuna.distri.tp_sockets.data.mappers.RegistrarConsumoDto;
import py.edu.fpuna.distri.tp_sockets.data.mappers.RegistrarConsumoResponse;
import py.edu.fpuna.distri.tp_sockets.data.repositories.MockSuministroRepository;
import py.edu.fpuna.distri.tp_sockets.domain.entities.EstadoActual;
import py.edu.fpuna.distri.tp_sockets.domain.entities.Suministro;
import py.edu.fpuna.distri.tp_sockets.domain.repositories.SuministroRepository;

public class AppServerTCP {
    private static Map<String, Suministro> initDB() {
        Map<String, Suministro> bdLocal = new HashMap<>();
        bdLocal.put("123", new Suministro("123", "Juan Pereria", 1234.56, 0, EstadoActual.ACTIVO));
        bdLocal.put("123456789", new Suministro("132423456789", "Juan Perez", 1234.5, 0, EstadoActual.ACTIVO));
        bdLocal.put("987654321", new Suministro("987634554321", "Maria Ramirez", 10000, 12345.6, EstadoActual.ACTIVO));
        bdLocal.put("123456789", new Suministro("1234567456389", "Juan Sosa", 1234.5, 12345.6, EstadoActual.ACTIVO));
        bdLocal.put("987654321", new Suministro("98765432561", "Mario Gomez", 1234.5, 12345.6, EstadoActual.ACTIVO));
        bdLocal.put("123456789", new Suministro("123456767589", "Juan Torres Brizuela", 12671, 0, EstadoActual.ACTIVO));
        bdLocal.put("987654321", new Suministro("98765432341", "Mariano Lopez", 1234.5, 0, EstadoActual.ACTIVO));
        bdLocal.put("123456789",
                new Suministro("12345678555559", "Enrique Gimenez", 1234.5, 12345.6, EstadoActual.ACTIVO));
        bdLocal.put("987654321", new Suministro("9876543266661", "Matias Lopez", 1234.5, 12345.6, EstadoActual.ACTIVO));
        bdLocal.put("123456789", new Suministro("123456787779", "Matias Perez", 1234.5, 12345.6, EstadoActual.ACTIVO));
        bdLocal.put("987654321",
                new Suministro("9876543286751", "Matias Pedroso", 1234.5, 12345.6, EstadoActual.ACTIVO));
        bdLocal.put("123456789", new Suministro("12345678564339", "Alejendro Sanz", 1234.5, 0, EstadoActual.ACTIVO));
        bdLocal.put("987654321", new Suministro("987654326541", "Enrique Iglesias", 1234.5, 0, EstadoActual.ACTIVO));
        bdLocal.put("123456789", new Suministro("12345678654549", "Arturo Suarez", 1234.5, 0, EstadoActual.ACTIVO));
        bdLocal.put("987654321",
                new Suministro("999", "Geronimo Torres", 1234.5, 12345.6, EstadoActual.INACTIVO));
        bdLocal.put("123456789",
                new Suministro("1234567875464569", "Juan Troche", 1234.5, 12345.6, EstadoActual.ACTIVO));
        bdLocal.put("987654321",
                new Suministro("987654326456451", "Maria Castillo", 1234.5, 12345.6, EstadoActual.INACTIVO));

        return bdLocal;
    }

    public static void main(String[] args) {

        ServerSocket serverSocket = null;
        Socket clientSocket = null;
        final int port = 9876;
        int tiempo_procesamiento_miliseg = 2000;

        try {
            tiempo_procesamiento_miliseg = Integer.parseInt(args[0]);
        } catch (Exception e1) {
            System.out.println(
                    "Se omite el argumento, tiempo de procesamiento " + tiempo_procesamiento_miliseg + ". Ref: " + e1);
        }

        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Servidor escuchando en puerto: " + port);
            PrintWriter out = null;
            BufferedReader in = null;
            Map<String, Suministro> bdLocal = initDB();
            SuministroRepository suministroRepository = new MockSuministroRepository(bdLocal);

            while (true) {
                System.out.println("Esperando NIS...");
                clientSocket = serverSocket.accept();

                System.out
                        .println("Conexion establecida con el NIS de: "
                                + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort());

                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                String inputLine, outputLine;
                inputLine = in.readLine();
                System.out.println("Request: " + inputLine);

                RegistrarConsumoDto registrarConsumoDto = RegistrarConsumoDto.fromJson(inputLine);

                String nis = registrarConsumoDto.getNis();

                int tipoOperacion = registrarConsumoDto.getIdOperacion();

                switch (tipoOperacion) {
                    // Registrar Consumo
                    case 1:
                        double consumo = registrarConsumoDto.getConsumo();
                        Suministro suministro = suministroRepository.registrarConsumo(nis, consumo);

                        if (suministro == null) {
                            System.out.println("El suministro no existe");
                            RegistrarConsumoResponse suministroModel = new RegistrarConsumoResponse("ok", 0,
                                    tipoOperacion);
                            outputLine = suministroModel.toJson();

                        } else {
                            RegistrarConsumoDataResponse registrarConsumoDataResponse = new RegistrarConsumoDataResponse(
                                    suministro.getNis(), suministro.getConsumo());
                            RegistrarConsumoResponse suministroModel = new RegistrarConsumoResponse("ok", 0,
                                    tipoOperacion,
                                    registrarConsumoDataResponse);

                            outputLine = suministroModel.toJson();
                        }

                        break;

                    // Verificar Conectividad
                    case 2:
                        boolean isConnected = suministroRepository.verificarConectividad(nis);
                        System.out.println("El suministro esta conectado: " + isConnected);

                        break;

                    // Enviar Orden de Desconexion
                    case 3:
                        Suministro suministro1 = suministroRepository.enviarOrdenDesconexion(nis);

                        if (suministro1 == null) {
                            System.out.println("El suministro no existe");
                            EnviarOrdenResponse suministroModel = new EnviarOrdenResponse("ok", 0, tipoOperacion);
                            outputLine = suministroModel.toJson();

                        } else {
                            EnviarOrdenDataResponse enviarOrdenDataResponse = new EnviarOrdenDataResponse(
                                    suministro1.getNis(), suministro1.getDeuda());

                            EnviarOrdenResponse suministroModel = new EnviarOrdenResponse("ok", 0, tipoOperacion,
                                    enviarOrdenDataResponse);

                            outputLine = suministroModel.toJson();
                        }
                        break;

                    // Enviar Orden de Conexion
                    case 4:
                        Suministro suministro2 = suministroRepository.enviarOrdenConexion(nis);

                        if (suministro2 == null) {
                            System.out.println("El suministro no existe");
                            EnviarOrdenResponse suministroModel = new EnviarOrdenResponse("ok", 0, tipoOperacion);
                            outputLine = suministroModel.toJson();

                        } else {
                            EnviarOrdenDataResponse enviarOrdenDataResponse = new EnviarOrdenDataResponse(
                                    suministro2.getNis(), suministro2.getDeuda());

                            EnviarOrdenResponse suministroModel = new EnviarOrdenResponse("ok", 0, tipoOperacion,
                                    enviarOrdenDataResponse);

                            outputLine = suministroModel.toJson();
                        }
                        break;

                    // Listar Suministros Activos
                    case 5:
                        List<Suministro> suministros = suministroRepository.listarSuministrosActivos();

                        ListarSuministroResponse listarSuministroResponseA = new ListarSuministroResponse("ok", 0,
                                tipoOperacion, suministros);

                        outputLine = listarSuministroResponseA.toJson();

                        System.out.println("Se envio la respuesta al NIS:" + nis);
                        System.out.println("________________________________________________");
                        System.out.println();
                        System.out.println(new String(outputLine).trim());

                        break;

                    // Listar Suministros Inactivos
                    case 6:
                        List<Suministro> suministrosInactivos = suministroRepository.listarSuministrosInactivos();
                        ListarSuministroResponse listarSuministroResponseI = new ListarSuministroResponse("ok", 0,
                                tipoOperacion, suministrosInactivos);

                        outputLine = listarSuministroResponseI.toJson();

                        System.out.println("Se envio la respuesta al NIS:" + nis);
                        System.out.println("________________________________________________");
                        System.out.println();
                        System.out.println(new String(outputLine).trim());

                        break;

                    default:
                        throw new IllegalArgumentException("Operacion no Valido: " + tipoOperacion);
                }

                outputLine = "Respuesta igual al recibido: " + inputLine;

                TimeUnit.MILLISECONDS.sleep(tiempo_procesamiento_miliseg);

                out.println(outputLine);




                clientSocket.close();

                System.out.println("Cerrando conexion con el cliente");

            }

        } catch (IOException e) {
            System.err.println("No se puede abrir el puerto: " + port);
            System.exit(1);
        } catch (InterruptedException e) {
            System.err.println("Error en el tiempo de procesamiento: " + e.getMessage());
            System.exit(1);
        }

    }

}
