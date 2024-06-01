class Cliente:
    def __init__(self, nombre=None, apellido=None, telefono=None, identificador=None, activo=True):
        self.nombre = nombre
        self.apellido = apellido
        self.telefono = telefono
        self.identificador = identificador
        self.activo = activo

    @staticmethod
    def crear_cliente():
        """
        Método que crea un cliente con los datos introducidos por el usuario.
        """
        try:
            nombre = input("Ingresa el nombre del cliente: ")
            apellido = input("Ingresa el apellido del cliente: ")
            telefono = input("Ingresa el teléfono del cliente: ")
            identificador = input("Ingresa el identificador del cliente: ")
            return Cliente(nombre, apellido, telefono, identificador)
        except ValueError as Err:
            print("Error: " + str(Err))

    def agregar_cliente(self, centro):
        """
        Método que agrega un cliente a la lista de clientes del centro.
        - self: instancia de la clase Cliente
        - centro: instancia de la clase Centro
        """
        if self not in centro.lista_clientes:
            centro.lista_clientes.append(self)
            print("Cliente agregado al centro exitosamente.")
        else:
            print("El cliente ya está registrado en el centro.")

    def quitar_cliente(self, centro):
        """
        Método que elimina un cliente de la lista de clientes del centro.
        No se podrá quitar si el cliente tiene reservas pendientes.
        - self: instancia de la clase Cliente
        - centro: instancia de la clase Centro
        """
        if self in centro.lista_clientes:
            if not self.tiene_reservas_pendientes(centro):
                centro.lista_clientes.remove(self)
                print("Cliente eliminado del centro exitosamente.")
            else:
                print("El cliente no se puede eliminar porque tiene reservas pendientes.")
        else:
            print("El cliente no está registrado en el centro.")

    def tiene_reservas_pendientes(self, centro):
        """
        Método que verifica si un cliente tiene reservas pendientes en el centro.
        - self: instancia de la clase Cliente
        - centro: instancia de la clase Centro
        """
        for reserva in centro.lista_reservas:
            if reserva.cliente == self and reserva.activa:
                return True
        return False

    @staticmethod
    def listar_clientes_morosos(centro):
        """
        Método que lista los clientes morosos del centro.
        - centro: instancia de la clase Centro
        """
        print("Clientes morosos:")
        for cliente in centro.lista_clientes:
            if cliente.activo is False:
                print(f"Nombre: {cliente.nombre}, Apellido: {cliente.apellido}")

    def __str__(self):
        return f"Nombre: {self.nombre}, Apellido: {self.apellido}, Teléfono: {self.telefono}, Identificador: {self.identificador}, Activo: {self.activo}"
