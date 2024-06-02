from cancha import Cancha

class Cliente:
    def __init__(self, nombre=None, apellido=None, telefono=None, identificador=None, activo=True):
        self.nombre = nombre
        self.apellido = apellido
        self.telefono = telefono
        self.identificador = identificador
        self.activo = activo
        self.saldo = 0
        self.movimientos = []

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
        if self not in centro.clientes:
            centro.clientes.append(self)
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
        if self in centro.clientes:
            if not self.tiene_reservas_pendientes(centro):
                centro.clientes.remove(self)
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
        for reserva in centro.reservas:
            if reserva.cliente == self and reserva.activa:
                return True
        return False

    def registrar_pago(self, monto):
        """
        Registra un pago del cliente y actualiza su saldo.
        """
        if monto > 0:
            self.saldo += monto
            self.movimientos.append(("Pago", monto))  # Registrar el movimiento
            print(f"Pago de {monto} registrado. Nuevo saldo: {self.saldo}")
        else:
            print("El monto del pago debe ser positivo.")
            
    def mostrar_saldo(self):
        """
        Muestra el saldo actual del cliente y sus movimientos.
        """
        print(f"Saldo actual: {self.saldo}")
        print("\nMovimientos:")
        for tipo, monto in self.movimientos:
            print(f"- {tipo}: {monto}")
    
    @staticmethod
    def listar_clientes_morosos(centro):
        """
        Método que lista los clientes morosos del centro.
        - centro: instancia de la clase Centro
        """
        print("Clientes morosos:")
        for cliente in centro.clientes:
            if cliente.activo is False:
                print(f"Nombre: {cliente.nombre}, Apellido: {cliente.apellido}")

    def __str__(self):
        return f"Nombre: {self.nombre}, Apellido: {self.apellido}, Teléfono: {self.telefono}, Identificador: {self.identificador}, Activo: {self.activo}"
#--------------------------------------------------------------------------------------------------------------------------------------------------------------------

class Empleado:
    def __init__(self, nombre=None, apellido=None):
        self.nombre = nombre
        self.apellido = apellido
        self.desocupado = True
        self.lista_tareas = []
        self.cancha_asignada = None

    @staticmethod
    def crear_empleado():
        """
        Método que crea un empleado con los datos introducidos por el usuario.
        """
        try:
            nombre = input("Ingresa el nombre del empleado: ")
            apellido = input("Ingresa el apellido del empleado: ")
            return Empleado(nombre, apellido)
        except ValueError as Err:
            print("Error: " + str(Err))

    def registrar_en_cancha(self, cancha):
        """
        Método que registra un empleado a una cancha.
        Para esto la cancha debe estar habilitada y el empleado no debe estar registrado en ninguna otra cancha.
        - self: instancia de la clase Empleado
        - cancha: instancia de la clase Cancha
        """
        if cancha.habilitada and self.cancha_asignada is None:
            cancha.lista_empleados.append(self)
            self.cancha_asignada = cancha
            self.desocupado = False
            print(f"Empleado {self.nombre} {self.apellido} registrado en la cancha {cancha.numero_cancha}.")
        else:
            print("No se puede registrar el empleado. La cancha no está habilitada o el empleado ya está asignado a otra cancha.")

    def asignar_tarea(self, tarea):
        """
        Método que asigna una tarea al empleado.
        - self: instancia de la clase Empleado
        - tarea: tarea a asignar (cadena de texto)
        """
        self.lista_tareas.append(tarea)
        self.desocupado = False
        print(f"Tarea '{tarea}' asignada a {self.nombre} {self.apellido}.")

    def quitar_tarea(self, tarea):
        """
        Método que quita una tarea del empleado.
        - self: instancia de la clase Empleado
        - tarea: tarea a quitar (cadena de texto)
        """
        if tarea in self.lista_tareas:
            self.lista_tareas.remove(tarea)
            if not self.lista_tareas:
                self.desocupado = True
            print(f"Tarea '{tarea}' quitada a {self.nombre} {self.apellido}.")
        else:
            print(f"La tarea '{tarea}' no está asignada al empleado {self.nombre} {self.apellido}.")

    def quitar_de_cancha(self, cancha):
        """
        Método que quita un empleado de la cancha.
        - self: instancia de la clase Empleado
        - cancha: instancia de la clase Cancha
        """
        if self in cancha.lista_empleados:
            cancha.lista_empleados.remove(self)
            self.cancha_asignada = None
            if not self.lista_tareas:
                self.desocupado = True
            print(f"Empleado {self.nombre} {self.apellido} quitado de la cancha {cancha.numero_cancha}.")
        else:
            print(f"El empleado {self.nombre} {self.apellido} no está registrado en la cancha {cancha.numero_cancha}.")

    @staticmethod
    def listar_empleados_desocupados(centro):
        """
        Método que lista los empleados desocupados del centro.
        - centro: instancia de la clase Centro
        """
        print("Empleados desocupados:")
        for empleado in centro.empleados:
            if empleado.desocupado:
                print(f"Nombre: {empleado.nombre}, Apellido: {empleado.apellido}")

    def __str__(self):
        return f"Nombre: {self.nombre}, Apellido: {self.apellido}, Desocupado: {self.desocupado}, Cancha Asignada: {self.cancha_asignada.numero_cancha if self.cancha_asignada else 'Ninguna'}"
