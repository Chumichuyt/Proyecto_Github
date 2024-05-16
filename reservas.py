class Reserva:
    def __init__(self, numero_reserva, fecha, cliente, cancha):
        self.numero_reserva = numero_reserva
        self.fecha = fecha
        self.cliente = cliente
        self.cancha = cancha

    @staticmethod
    def crear_reserva(numero_reserva, fecha, cliente, cancha, lista_reservas):
        
        # Verificar si la cancha está habilitada

        # Verificar si el cliente está habilitado
            # Y saldo negativo no menor a -2000

        # Crear la reserva
        reserva = Reserva(numero_reserva, fecha, cliente, cancha)
        lista_reservas.append(reserva)
        print("Reserva creada exitosamente.")
        return reserva
    
    @staticmethod
    def cancha_disponible(cancha, fecha, lista_reservas):
        
        for reserva in lista_reservas:
            if reserva.cancha == cancha and reserva.fecha == fecha:
                return False
        return True

    @staticmethod
    def listar_reservas_cancha(cancha, lista_reservas):
        
        print(f"Reservas para la cancha {cancha.numero_cancha}:")
        for reserva in lista_reservas:
            if reserva.cancha == cancha:
                print(f"Número de reserva: {reserva.numero_reserva}, Fecha: {reserva.fecha}, Cliente: {reserva.cliente.nombre}")

    @staticmethod
    def listar_reservas_cliente(cliente, lista_reservas):
        
        print(f"Reservas para el cliente {cliente.nombre}:")
        for reserva in lista_reservas:
            if reserva.cliente == cliente:
                print(f"Número de reserva: {reserva.numero_reserva}, Fecha: {reserva.fecha}, Cancha: {reserva.cancha.numero_cancha}")
    