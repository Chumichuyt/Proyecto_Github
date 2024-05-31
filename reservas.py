class Reserva:
    def __init__(self, numero_reserva=None, fecha=None, cliente=None, cancha=None):
        self.numero_reserva = numero_reserva
        self.fecha = fecha
        self.cliente = cliente
        self.cancha = cancha

    def crear_reserva(numero_reserva, fecha, cliente, cancha, lista_reservas):
        
        # Verificar si la cancha está habilitada
        if not Reserva.cancha_disponible(cancha, fecha, lista_reservas):
            print("La cancha no está disponible para la fecha seleccionada.")
            return None
        
        # Verificar si el cliente está habilitado
            # Y saldo negativo no menor a -2000
            # Pendiente de implementar con módulo persona.py
            
        if not Reserva.cliente_habilitado(cliente, lista_reservas): 
            print("El cliente no está habilitado.")
            return None
        
        # Crear la reserva
        reserva = Reserva(numero_reserva, fecha, cliente, cancha)
        lista_reservas.append(reserva)
        print("Reserva creada exitosamente.")
        return reserva
    
    @staticmethod
    def cliente_habilitado(self, cliente, lista_reservas):
        """
            Método para comprobar si un cliente está habilitado.
        """
        for reserva in lista_reservas:
            if reserva.cliente == cliente:
                return True
        return False
    
    @staticmethod
    def cancha_disponible(cancha, fecha, lista_reservas):
        """
            Método para comprobar si una cancha está habilitada para una fecha dada.
        """
        for reserva in lista_reservas:
            if reserva.cancha == cancha and reserva.fecha == fecha:
                return False
        return True

    def listar_reservas_cancha(cancha, lista_reservas):
        """
            Método para mostrar las reservas de una cancha.
        """
        print(f"Reservas para la cancha {cancha.numero_cancha}:")
        for reserva in lista_reservas:
            if reserva.cancha == cancha:
                print(f"Número de reserva: {reserva.numero_reserva}, Fecha: {reserva.fecha}, Cliente: {reserva.cliente.nombre}")

    def listar_reservas_cliente(cliente, lista_reservas):
        """
            Método para mostrar las reservas de un cliente.
        """
        print(f"Reservas para el cliente {cliente.nombre}:")
        for reserva in lista_reservas:
            if reserva.cliente == cliente:
                print(f"Número de reserva: {reserva.numero_reserva}, Fecha: {reserva.fecha}, Cancha: {reserva.cancha.numero_cancha}")

    def __str__(self):
        return f"Reserva: {self.numero_reserva}, Fecha: {self.fecha}, Cliente: {self.cliente}, Cancha: {self.cancha}"
    