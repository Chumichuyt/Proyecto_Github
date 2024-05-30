from cancha import Cancha
from reservas import Reserva
#from persona import Persona

class Centro:
    def __init__(self, nombre, direccion):
        self.nombre = nombre
        self.direccion = direccion
        self.lista_canchas = []
        self.lista_clientes = []

