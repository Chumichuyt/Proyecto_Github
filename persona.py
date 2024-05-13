
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
        Método estático para crear un cliente con los datos introducidos por el usuario.
        """
        nombre = input("Ingrese el nombre del cliente: ")
        apellido = input("Ingrese el apellido del cliente: ")
        telefono = input("Ingrese el número de teléfono del cliente: ")
        identificador = input("Ingrese el identificador del cliente: ")
        activo = input("¿El cliente está activo? (Si/No): ").lower() == "si"
        return Cliente(nombre, apellido, telefono, identificador, activo)

    def agregar_a_centro(self, lista_clientes):
        """
        Método para agregar este cliente a la lista del centro.
        """
        if self not in lista_clientes:
            lista_clientes.append(self)
            print("Cliente agregado al centro exitosamente.")
        else:
            print("El cliente ya está registrado en el centro.")

    def quitar_del_centro(self, reservas_pendientes):
        """
        Método para quitar este cliente de la lista del centro si no tiene reservas pendientes.
        :param reservas_pendientes: Booleano indicando si el cliente tiene reservas pendientes
        """
        if reservas_pendientes:
            print("El cliente tiene reservas pendientes. No se puede quitar.")
        else:
            print("Cliente quitado del centro exitosamente.")

    @staticmethod
    def listar_clientes_morosos(lista_clientes):
        """
        Método estático para listar clientes morosos de una lista de clientes.
        :param lista_clientes: Lista de clientes
        """
        print("Listado de clientes morosos:")
        for cliente in lista_clientes:
            if not cliente.activo:
                print(f"{cliente.nombre} {cliente.apellido}")
